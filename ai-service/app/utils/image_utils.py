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


async def get_user_img_url(uuid: str, db: AsyncSession):
    try:
        user = await db.execute(select(FaceData).filter(FaceData.user_id == uuid))
        user_record = user.scalars().first()

        if not user_record:
            return JSONResponse(
                status_code=404,
                content=error_response(
                    error="Not Found",
                    message="Face verification has not been set up for this user",
                    status=404,
                    path="/face/verify"
                )
            )

        return user_record.img_url

    except Exception as e:
        return JSONResponse(
            status_code=400,
            content=error_response(
                error="Bad Request",
                message=f"Invalid UUID format: {str(e)}",
                status=400,
                path="/face/verify"
            )
        )


async def calculate_similarity_for_user(uuid: str, image: bytes, db: AsyncSession) -> Dict[str, Any]:
    try:
        url_list = await get_user_img_url(uuid, db)

        if isinstance(url_list, JSONResponse):
            return url_list
        
        if len(url_list) == 0:
            return JSONResponse(
                status_code=404,
                content=error_response(
                    error="Not Found",
                    message=f"No face data found for user {uuid}",
                    status=404,
                    path="/face/verify"
                )
            )

        server_images = []
        errors = []

        for url in url_list:
            try:
                img = read_image_from_url(url)
                server_images.append(img)
            except Exception as e:
                errors.append(f"Failed to fetch image {url}: {str(e)}")

        if len(server_images) == 0:
            return JSONResponse(
                status_code=500,
                content=error_response(
                    error="Processing Failed",
                    message="Failed to download any face images for this user",
                    status=500,
                    path="/face/verify",
                    validation_errors={"download_errors": errors}
                )
            )

        result = await verify_from_service(server_images, image)

        if not result.get("success"):
            return JSONResponse(
                status_code=500,
                content=error_response(
                    error="Face Verification Failed",
                    message=result.get("error"),
                    status=500,
                    path="/face/verify"
                )
            )

        data = result["data"]

        avg_sim = data["similarity_avg"]
        match = data["match"]

        return success_response(
            data={
                "similarity": avg_sim,
                "match": match,
                "images_processed": data["count_valid"],
                "total_images": len(url_list),
                "errors": errors + (data["errors"] or [])
            }
        )

    except HTTPException:
        raise

    except Exception as e:
        print(repr(e))
        print(type(e))
        print(e.args)
        return JSONResponse(
            status_code=500,
            content=error_response(
                error="Internal Server Error",
                message=str(e),
                status=500,
                path="/face/verify"
            )
        )
