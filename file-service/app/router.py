import cloudinary
import cloudinary.uploader
import cloudinary.api
from fastapi import APIRouter, Form, UploadFile, File, status, Request
from fastapi.responses import JSONResponse
from datetime import datetime
from typing import List
from .schemas import ErrorResponse, UploadResponse, DownloadResponse, MultipleUploadResponse, UploadedFileInfo

router = APIRouter(tags=["File Operations"])


def create_error_response(
    error: str,
    message: str,
    status_code: int,
    path: str = None,
    validation_errors: dict = None
) -> JSONResponse:
    """Create standardized error response"""
    error_data = ErrorResponse(
        error=error,
        message=message,
        status=status_code,
        timestamp=datetime.now(),
        path=path,
        validationErrors=validation_errors
    )
    return JSONResponse(
        status_code=status_code,
        content=error_data.model_dump(exclude_none=True, by_alias=True, mode='json')
    )


@router.post("/upload", status_code=status.HTTP_201_CREATED, response_model=UploadResponse)
async def upload_file(
    request: Request,
    uid: str = Form(..., description="User ID"),
    file: UploadFile = File(..., description="File to upload")
):
    """
    Upload file to Cloudinary
    
    - **uid**: User ID for organizing files
    - **file**: File to upload (images, documents, etc.)
    """
    try:
        if not file.filename:
            return create_error_response(
                error="Bad Request",
                message="No file provided",
                status_code=status.HTTP_400_BAD_REQUEST,
                path=str(request.url.path)
            )
        
        result = cloudinary.uploader.upload(
            file.file,
            folder=f"users/{uid}",
            public_id=f"file_{uid}_{datetime.now().timestamp()}",
            overwrite=True,
            resource_type="auto" 
        )
        
        preview_url = cloudinary.CloudinaryImage(result["public_id"]).build_url(
            transformation=[
                {"width": 400, "height": 400, "crop": "limit"},
                {"quality": "auto"},
                {"fetch_format": "auto"}
            ]
        )
        
        return UploadResponse(
            message="Upload successful",
            user_id=uid,
            url=result["secure_url"],
            public_id=result["public_id"],
            preview_url=preview_url
        )

    except cloudinary.exceptions.Error as e:
        return create_error_response(
            error="Cloudinary Error",
            message=f"Upload failed: {str(e)}",
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            path=str(request.url.path)
        )
    except Exception as e:
        return create_error_response(
            error="Internal Server Error",
            message=f"Upload failed: {str(e)}",
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            path=str(request.url.path)
        )


@router.post("/upload-multiple", status_code=status.HTTP_201_CREATED, response_model=MultipleUploadResponse)
async def upload_multiple_files(
    request: Request,
    uid: str = Form(..., description="User ID"),
    files: List[UploadFile] = File(..., description="Multiple files to upload")
):
    """
    Upload multiple files to Cloudinary
    
    - **uid**: User ID for organizing files
    - **files**: Multiple files to upload (images, documents, etc.)
    """
    if not files:
        return create_error_response(
            error="Bad Request",
            message="No files provided",
            status_code=status.HTTP_400_BAD_REQUEST,
            path=str(request.url.path)
        )
    
    uploaded_files = []
    errors = []
    successful_count = 0
    failed_count = 0
    
    for file in files:
        try:
            if not file.filename:
                errors.append(f"Empty filename in upload list")
                failed_count += 1
                continue
            
            result = cloudinary.uploader.upload(
                file.file,
                folder=f"users/{uid}",
                public_id=f"file_{uid}_{datetime.now().timestamp()}",
                overwrite=True,
                resource_type="auto" 
            )
            
            preview_url = cloudinary.CloudinaryImage(result["public_id"]).build_url(
                transformation=[
                    {"width": 400, "height": 400, "crop": "limit"},
                    {"quality": "auto"},
                    {"fetch_format": "auto"}
                ]
            )
            
            uploaded_files.append(UploadedFileInfo(
                url=result["secure_url"],
                public_id=result["public_id"],
                preview_url=preview_url,
                filename=file.filename
            ))
            successful_count += 1
            
        except cloudinary.exceptions.Error as e:
            errors.append(f"Failed to upload {file.filename}: {str(e)}")
            failed_count += 1
        except Exception as e:
            errors.append(f"Failed to upload {file.filename}: {str(e)}")
            failed_count += 1
    
    return MultipleUploadResponse(
        message=f"Upload completed: {successful_count} successful, {failed_count} failed",
        user_id=uid,
        total_files=len(files),
        successful_uploads=successful_count,
        failed_uploads=failed_count,
        files=uploaded_files,
        errors=errors if errors else None
    )


@router.get("/download/{public_id:path}", response_model=DownloadResponse)
async def download_file(request: Request, public_id: str):
    """
    Get download URL and preview URL for a file
    
    - **public_id**: Public ID of the file in Cloudinary (e.g., 'users/file_user123_1234567890')
    """
    try:
        resource = None
        for res_type in ["image", "video", "raw"]:
            try:
                resource = cloudinary.api.resource(public_id, resource_type=res_type)
                break
            except cloudinary.exceptions.NotFound:
                continue
        
        if resource is None:
            return create_error_response(
                error="Not Found",
                message=f"File not found with public_id: {public_id}",
                status_code=status.HTTP_404_NOT_FOUND,
                path=str(request.url.path)
            )
        download_url = cloudinary.CloudinaryImage(public_id).build_url(
            resource_type=resource.get("resource_type", "image"),
            type=resource.get("type", "upload"),
            flags="attachment" 
        )
        
        preview_url = cloudinary.CloudinaryImage(public_id).build_url(
            transformation=[
                {"width": 800, "height": 800, "crop": "limit"},
                {"quality": "auto"},
                {"fetch_format": "auto"}
            ]
        )
        
        return DownloadResponse(
            message="File retrieved successfully",
            url=download_url,
            preview_url=preview_url,
            public_id=resource["public_id"],
            resource_type=resource.get("resource_type", "image"),
            format=resource.get("format", "unknown"),
            created_at=resource.get("created_at", "")
        )
        
    except cloudinary.exceptions.Error as e:
        return create_error_response(
            error="Cloudinary Error",
            message=f"Failed to retrieve file: {str(e)}",
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            path=str(request.url.path)
        )
    except Exception as e:
        return create_error_response(
            error="Internal Server Error",
            message=f"Failed to retrieve file: {str(e)}",
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            path=str(request.url.path)
        )


@router.delete("/delete/{public_id:path}")
async def delete_file(request: Request, public_id: str):
    """
    Delete a file from Cloudinary
    
    - **public_id**: Public ID of the file to delete
    """
    try:
        result = None
        for res_type in ["image", "video", "raw"]:
            try:
                result = cloudinary.uploader.destroy(public_id, resource_type=res_type, invalidate=True)
                if result.get("result") == "ok":
                    break
            except cloudinary.exceptions.Error:
                continue
        
        if result and result.get("result") == "ok":
            return {
                "message": "File deleted successfully",
                "public_id": public_id
            }
        elif result and result.get("result") == "not found":
            return create_error_response(
                error="Not Found",
                message=f"File not found with public_id: {public_id}",
                status_code=status.HTTP_404_NOT_FOUND,
                path=str(request.url.path)
            )
        else:
            return create_error_response(
                error="Delete Failed",
                message=f"Failed to delete file: {result.get('result', 'unknown error') if result else 'File not found'}",
                status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                path=str(request.url.path)
            )
            
    except cloudinary.exceptions.Error as e:
        return create_error_response(
            error="Cloudinary Error",
            message=f"Failed to delete file: {str(e)}",
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            path=str(request.url.path)
        )
    except Exception as e:
        return create_error_response(
            error="Internal Server Error",
            message=f"Failed to delete file: {str(e)}",
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            path=str(request.url.path)
        )
