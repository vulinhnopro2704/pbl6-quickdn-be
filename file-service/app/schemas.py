from datetime import datetime
from typing import Optional, Dict
from pydantic import BaseModel, Field


class ErrorResponse(BaseModel):
    """Standard error response structure"""
    error: str = Field(..., description="Error type/category", example="Bad Request")
    message: str = Field(..., description="Detailed error message", example="File not found")
    status: int = Field(..., description="HTTP status code", example=400)
    timestamp: datetime = Field(default_factory=datetime.now, description="Timestamp when the error occurred")
    path: Optional[str] = Field(None, description="Request path where the error occurred", example="/api/file/download")
    validationErrors: Optional[Dict[str, str]] = Field(None, description="Validation errors for field-specific issues")

    class Config:
        json_encoders = {
            datetime: lambda v: v.isoformat()
        }
        json_schema_extra = {
            "example": {
                "error": "Not Found",
                "message": "File not found with the given public_id",
                "status": 404,
                "timestamp": "2025-11-20T10:30:45",
                "path": "/api/file/download/test_file"
            }
        }


class UploadResponse(BaseModel):
    """Upload file response"""
    message: str = Field(..., example="Upload successful")
    user_id: str = Field(..., example="user123")
    url: str = Field(..., description="Secure URL to access the file")
    public_id: str = Field(..., description="Public ID of the uploaded file")
    preview_url: str = Field(..., description="Preview URL for the file")


class DownloadResponse(BaseModel):
    """Download file response"""
    message: str = Field(..., example="File retrieved successfully")
    url: str = Field(..., description="Direct download URL")
    preview_url: str = Field(..., description="Preview URL for the file")
    public_id: str = Field(..., description="Public ID of the file")
    resource_type: str = Field(..., example="image")
    format: str = Field(..., example="jpg")
    created_at: str = Field(..., description="File creation timestamp")


class UploadedFileInfo(BaseModel):
    """Information about a single uploaded file"""
    url: str = Field(..., description="Secure URL to access the file")
    public_id: str = Field(..., description="Public ID of the uploaded file")
    preview_url: str = Field(..., description="Preview URL for the file")
    filename: str = Field(..., description="Original filename")


class MultipleUploadResponse(BaseModel):
    """Multiple files upload response"""
    message: str = Field(..., example="Upload successful")
    user_id: str = Field(..., example="user123")
    total_files: int = Field(..., description="Total number of files uploaded")
    successful_uploads: int = Field(..., description="Number of successful uploads")
    failed_uploads: int = Field(..., description="Number of failed uploads")
    files: list[UploadedFileInfo] = Field(..., description="List of uploaded files information")
    errors: Optional[list[str]] = Field(None, description="List of errors if any")
