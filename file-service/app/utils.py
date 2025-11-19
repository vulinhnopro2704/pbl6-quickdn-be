from fastapi.responses import JSONResponse
from datetime import datetime
from typing import Any, Optional


def success_response(
    data: Any = None,
    message: str = "Success",
    status_code: int = 200
) -> JSONResponse:
    """
    Tạo success response chuẩn
    
    Args:
        data: Dữ liệu trả về
        message: Thông báo thành công
        status_code: HTTP status code (default: 200)
    """
    return JSONResponse(
        status_code=status_code,
        content={
            "success": True,
            "status_code": status_code,
            "message": message,
            "timestamp": datetime.utcnow().isoformat(),
            "data": data,
            "error": None
        }
    )


def error_response(
    message: str,
    status_code: int = 400,
    error_detail: Optional[str] = None
) -> JSONResponse:
    """
    Tạo error response chuẩn
    
    Args:
        message: Thông báo lỗi chính
        status_code: HTTP status code
        error_detail: Chi tiết lỗi (optional)
    """
    return JSONResponse(
        status_code=status_code,
        content={
            "success": False,
            "status_code": status_code,
            "message": message,
            "timestamp": datetime.utcnow().isoformat(),
            "data": None,
            "error": error_detail or message
        }
    )
