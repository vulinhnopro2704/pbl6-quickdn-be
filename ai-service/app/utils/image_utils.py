from typing import List, Dict, Any
from fastapi import Depends, HTTPException
from fastapi.responses import JSONResponse
import requests
from sqlalchemy import select
from app.config.settings import FACE_SIMILARITY_THRESHOLD
from app.types.face_data import FaceData
from sqlalchemy.ext.asyncio import AsyncSession
from app.config.database import get_async_db
from app.services.face_service import verify_from_service
from app.types.response import error_response, success_response

def read_image_from_url(url: str) -> bytes:
    r = requests.get(url, stream=True)
    r.raise_for_status()
    return r.content

async def get_user_img_url(uuid: str, db: AsyncSession) -> List[str]:
    try:
        user = await db.execute(select(FaceData).filter(FaceData.user_id == uuid))
        url_list = []
        user_record = user.scalars().first()
        if user_record:
            url_list = user_record.img_url
        else:
            return JSONResponse(
                status_code=404,
                content=error_response(
                    error="Not Found",
                    message="Face verification has not been set up for this user",
                    status=404,
                    path="/face/verify"
                )
            )
        return url_list
    except JSONResponse as json_resp:
        return json_resp
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content=error_response(
                error="Internal Server Error",
                message="Not valid uuid format",
                status=500,
                path="/face/verify"
            )
        )

async def calculate_similarity_for_user(uuid: str, image: bytes, db: AsyncSession) -> Dict[str, Any]:
    try:
        url_list = await get_user_img_url(uuid, db)
        
        if not url_list:
            return JSONResponse(
                status_code=404,
                content=error_response(
                    error="Not Found",
                    message=f"No face data found for user {uuid}",
                    status=404,
                    path="/face/verify"
                )
            )
        
        sum_similarity = 0.0
        count = 0
        errors = []
        
        for url in url_list:
            try:
                server_image = read_image_from_url(url)
                result = await verify_from_service(server_image, image)
                
                if not result.get('success'):
                    errors.append(f"Service error for {url}: {result.get('error')}")
                    continue
                
                similarity_score = result['data']['similarity']
                sum_similarity += similarity_score
                count += 1
            except requests.exceptions.RequestException as e:
                errors.append(f"Failed to fetch image from {url}: {str(e)}")
            except KeyError as e:
                errors.append(f"Invalid response format for {url}: missing key {str(e)}")
            except Exception as e:
                errors.append(f"Error processing image from {url}: {str(e)}")
        
        if count == 0:
            return JSONResponse(
                status_code=500,
                content=error_response(
                    error="Processing Failed",
                    message="Failed to process any face images",
                    status=500,
                    path="/face/verify",
                    validation_errors={"processing_errors": ", ".join(errors)}
                )
            )
        
        avg_similarity = sum_similarity / count
        
        return success_response(
            data={
                "similarity": avg_similarity,
                "match": avg_similarity > FACE_SIMILARITY_THRESHOLD,
                "images_processed": count,
                "total_images": len(url_list),
                "errors": errors if errors else None
            }
        )
        
    except HTTPException:
        raise
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content=error_response(
                error="Internal Server Error",
                message=str(e),
                status=500,
                path="/face/verify"
            )
        )