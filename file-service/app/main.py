import os
import dotenv
import json
from fastapi import FastAPI, Form, UploadFile, File, HTTPException, Query, status
from fastapi.concurrency import asynccontextmanager
from fastapi.exceptions import RequestValidationError
from minio import Minio
from minio.error import S3Error
from datetime import datetime, timedelta
import uuid

from app.utils import success_response, error_response
from app.exceptions import (
    validation_exception_handler,
    general_exception_handler,
    minio_exception_handler
)
from app.schemas import UploadFileData, FileUrlData, HealthCheckData


MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT")
MINIO_ACCESS_KEY = os.getenv("MINIO_ACCESS_KEY")
MINIO_SECRET_KEY = os.getenv("MINIO_SECRET_KEY")
DEFAULT_BUCKET_NAME = os.getenv("DEFAULT_BUCKET_NAME")
MINIO_EXTERNAL_ENDPOINT = os.getenv("MINIO_EXTERNAL_ENDPOINT", "localhost:9000")

def create_bucket_if_not_exists(bucket_name: str):
    policy = {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Principal": {"AWS": "*"},
                "Action": ["s3:GetObject"],
                "Resource": [f"arn:aws:s3:::{bucket_name}/*"]
            }
        ]
    }
    
    if not minio_client.bucket_exists(bucket_name):
        minio_client.make_bucket(bucket_name)
        print(f"Bucket '{bucket_name}' created.")

    minio_client.set_bucket_policy(bucket_name, json.dumps(policy))
    print(f"Bucket '{bucket_name}' policy set to public read.")

@asynccontextmanager
async def lifespan(app: FastAPI):
    create_bucket_if_not_exists(DEFAULT_BUCKET_NAME)
    yield
    
app = FastAPI(title="File Service MINIO", lifespan=lifespan, docs_url="/api/docs", redoc_url="/api/redoc")

# Đăng ký exception handlers
app.add_exception_handler(RequestValidationError, validation_exception_handler)
app.add_exception_handler(S3Error, minio_exception_handler)
app.add_exception_handler(Exception, general_exception_handler)

minio_client = Minio(
    MINIO_ENDPOINT,
    access_key=MINIO_ACCESS_KEY,
    secret_key=MINIO_SECRET_KEY,
    secure=False,
)


@app.get("/api/health")
def health_check():
    """Health check endpoint với status MinIO connection"""
    try:
        # Kiểm tra kết nối MinIO
        minio_connected = minio_client.bucket_exists(DEFAULT_BUCKET_NAME)
        
        data = HealthCheckData(
            status="healthy" if minio_connected else "degraded",
            minio_connected=minio_connected
        )
        
        return success_response(
            data=data.dict(),
            message="Service is running",
            status_code=status.HTTP_200_OK
        )
    except Exception as e:
        return error_response(
            message="Service is unhealthy",
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            error_detail=str(e)
        )


@app.post("/api/upload")
async def upload_file(file: UploadFile = File(...), bucket_name: str = Form(None)):
    """
    Upload file lên MinIO storage
    
    Args:
        file: File cần upload
        bucket_name: Tên bucket (optional, dùng default nếu không có)
    
    Returns:
        Response với thông tin file đã upload
    """
    try:
        # Validate file
        if not file:
            return error_response(
                message="No file provided",
                status_code=status.HTTP_400_BAD_REQUEST,
                error_detail="File is required"
            )
        
        if not file.filename:
            return error_response(
                message="Invalid file",
                status_code=status.HTTP_400_BAD_REQUEST,
                error_detail="Filename is empty"
            )
        
        ALLOWED_IMAGE_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", 
            "image/webp", "image/bmp", "image/svg+xml", "image/tiff"
        }
        ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".svg", ".tiff", ".tif"}
        
        file_extension = os.path.splitext(file.filename)[1].lower()
        
        if file_extension not in ALLOWED_EXTENSIONS:
            return error_response(
                message="Invalid file type",
                status_code=status.HTTP_400_BAD_REQUEST,
                error_detail=f"Only image files are allowed. Supported formats: {', '.join(ALLOWED_EXTENSIONS)}"
            )
        
        if file.content_type and file.content_type not in ALLOWED_IMAGE_TYPES:
            return error_response(
                message="Invalid content type",
                status_code=status.HTTP_400_BAD_REQUEST,
                error_detail=f"Only image files are allowed. Received: {file.content_type}"
            )
        
        target_bucket = bucket_name or DEFAULT_BUCKET_NAME
        
        create_bucket_if_not_exists(target_bucket)
        
        unique_filename = f"{uuid.uuid4()}{file_extension}"
        
        file_content = await file.read()
        file_size = len(file_content)
        
        MAX_FILE_SIZE = 100 * 1024 * 1024  
        if file_size > MAX_FILE_SIZE:
            return error_response(
                message="File too large",
                status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                error_detail=f"Maximum file size is {MAX_FILE_SIZE / (1024*1024)}MB"
            )
        
        from io import BytesIO
        minio_client.put_object(
            bucket_name=target_bucket,
            object_name=unique_filename,
            data=BytesIO(file_content),
            length=file_size,
            content_type=file.content_type or "application/octet-stream"
        )
        
        public_url = f"http://{MINIO_EXTERNAL_ENDPOINT}/{target_bucket}/{unique_filename}"
        
        data = UploadFileData(
            filename=unique_filename,
            original_filename=file.filename,
            bucket=target_bucket,
            size=file_size,
            content_type=file.content_type,
            url=public_url
        )
        
        return success_response(
            data=data.dict(),
            message="File uploaded successfully",
            status_code=status.HTTP_201_CREATED
        )
    
    except S3Error as e:
        return error_response(
            message="Failed to upload file to storage",
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            error_detail=f"MinIO error: {e.message}"
        )
    except Exception as e:
        return error_response(
            message="Failed to upload file",
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            error_detail=str(e)
        )


@app.get("/api/file/{filename}")
def get_file_url(
    filename: str, 
    bucket_name: str = Query(None)
):
    """
    Lấy public URL của file
    
    Args:
        filename: Tên file đã lưu (UUID filename từ upload response)
        bucket_name: Tên bucket (optional)
    
    Returns:
        Response với URL của file
    """
    try:
        # Validate filename
        if not filename or filename.strip() == "":
            return error_response(
                message="Invalid filename",
                status_code=status.HTTP_400_BAD_REQUEST,
                error_detail="Filename cannot be empty"
            )
        
        target_bucket = bucket_name or DEFAULT_BUCKET_NAME
        try:
            minio_client.stat_object(target_bucket, filename)
        except S3Error as e:
            if e.code == "NoSuchKey":
                return error_response(
                    message="File not found",
                    status_code=status.HTTP_404_NOT_FOUND,
                    error_detail=f"File '{filename}' does not exist in bucket '{target_bucket}'"
                )
            elif e.code == "NoSuchBucket":
                return error_response(
                    message="Bucket not found",
                    status_code=status.HTTP_404_NOT_FOUND,
                    error_detail=f"Bucket '{target_bucket}' does not exist"
                )
            else:
                raise

        public_url = f"http://{MINIO_EXTERNAL_ENDPOINT}/{target_bucket}/{filename}"
        
        data = FileUrlData(
            url=public_url,
            filename=filename,
            bucket=target_bucket
        )
        
        return success_response(
            data=data.dict(),
            message="File URL retrieved successfully",
            status_code=status.HTTP_200_OK
        )
    
    except S3Error as e:
        return error_response(
            message="Failed to retrieve file information",
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            error_detail=f"MinIO error: {e.message}"
        )
    except Exception as e:
        return error_response(
            message="Failed to generate file URL",
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            error_detail=str(e)
        )