from fastapi import Request, status
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from datetime import datetime
from minio.error import S3Error


async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """Xử lý validation errors"""
    errors = []
    for error in exc.errors():
        errors.append({
            "field": " -> ".join(str(x) for x in error["loc"]),
            "message": error["msg"],
            "type": error["type"]
        })
    
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "success": False,
            "status_code": status.HTTP_422_UNPROCESSABLE_ENTITY,
            "message": "Validation Error",
            "timestamp": datetime.now().isoformat(),
            "data": None,
            "error": {
                "type": "validation_error",
                "details": errors
            }
        }
    )


async def general_exception_handler(request: Request, exc: Exception):
    """Xử lý các exception chung"""
    status_code = status.HTTP_500_INTERNAL_SERVER_ERROR
    error_type = type(exc).__name__
    
    return JSONResponse(
        status_code=status_code,
        content={
            "success": False,
            "status_code": status_code,
            "message": "Internal Server Error",
            "timestamp": datetime.now().isoformat(),
            "data": None,
            "error": {
                "type": error_type,
                "detail": str(exc)
            }
        }
    )


async def minio_exception_handler(request: Request, exc: S3Error):
    """Xử lý MinIO S3 errors"""
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "success": False,
            "status_code": status.HTTP_500_INTERNAL_SERVER_ERROR,
            "message": "Storage Service Error",
            "timestamp": datetime.utcnow().isoformat(),
            "data": None,
            "error": {
                "type": "minio_error",
                "code": exc.code,
                "detail": exc.message
            }
        }
    )
