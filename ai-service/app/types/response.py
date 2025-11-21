from datetime import datetime
from typing import Optional, Dict, Any

def error_response(
    error: str,
    message: str,
    status: int,
    path: Optional[str] = None,
    validation_errors: Optional[Dict[str, str]] = None
) -> Dict[str, Any]:
    """
    Create a standardized error response
    
    Args:
        error: Error type/category
        message: Detailed error message
        status: HTTP status code
        path: Request path where the error occurred
        validation_errors: Field-specific validation errors
    
    Returns:
        Dictionary with error response structure
    """
    response = {
        "error": error,
        "message": message,
        "status": status,
        "timestamp": datetime.now().isoformat()
    }
    
    if path:
        response["path"] = path
    
    if validation_errors:
        response["validationErrors"] = validation_errors
    
    return response


def success_response(data: Any, message: Optional[str] = None) -> Dict[str, Any]:
    """
    Create a standardized success response
    
    Args:
        data: Response data
        message: Optional success message
    
    Returns:
        Dictionary with success response structure
    """
    response = {
        "success": True,
        "data": data,
    }
    
    if message:
        response["message"] = message
    
    return response
