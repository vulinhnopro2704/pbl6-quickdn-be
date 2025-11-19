from pydantic import BaseModel
from typing import Optional, Any
from datetime import datetime


class BaseResponse(BaseModel):
    """Base response model cho tất cả API responses"""
    success: bool
    status_code: int
    message: str
    timestamp: str = datetime.now().isoformat()
    data: Optional[Any] = None
    error: Optional[str] = None


class UploadFileData(BaseModel):
    """Data model cho upload file response"""
    filename: str
    original_filename: str
    bucket: str
    size: int
    content_type: Optional[str]
    url: str


class FileUrlData(BaseModel):
    """Data model cho file URL response"""
    url: str
    filename: str
    bucket: str


class HealthCheckData(BaseModel):
    """Data model cho health check response"""
    status: str
    service: str = "File Service"
    minio_connected: bool
