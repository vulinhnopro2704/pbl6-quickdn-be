from fastapi import APIRouter, Form, UploadFile, File, status, HTTPException, Depends
from fastapi.responses import JSONResponse
from app.utils.image_utils import calculate_similarity_for_user
from sqlalchemy.ext.asyncio import AsyncSession
from app.config.database import get_async_db
from app.types.request import UploadFaceRequest
from app.types.response import error_response

router = APIRouter(prefix="/face", tags=["Face Recognition"])

@router.post("/verify", status_code=status.HTTP_200_OK)
async def verify_face(uid: str = Form(...), file: UploadFile = File(...), db: AsyncSession = Depends(get_async_db)):
    try:
        image = await file.read()
        res = await calculate_similarity_for_user(uid, image, db)
        return res
    except HTTPException:
        raise
    except Exception as e:
        from app.types.response import error_response
        from fastapi.responses import JSONResponse
        return JSONResponse(
            status_code=500,
            content=error_response(
                error="Internal Server Error",
                message=str(e),
                status=500,
                path="/face/verify"
            )
        )


@router.post("/upload/{uuid}", status_code=status.HTTP_200_OK)
async def upload_face(uuid: str, body: UploadFaceRequest, db: AsyncSession = Depends(get_async_db)):
    try:
        from app.types.face_data import FaceData
        from sqlalchemy import select
        from datetime import datetime
        
        url_list = body.url_list
        
        result = await db.execute(select(FaceData).where(FaceData.user_id == uuid))
        face_data = result.scalar_one_or_none()
        
        if face_data:
            existing_urls = face_data.img_url or []
            face_data.img_url = existing_urls + url_list
            face_data.updated_at = datetime.now()
            await db.commit()
            await db.refresh(face_data)
            res = {
                "success": True, 
                "message": "Appended images to existing user", 
                "user_id": str(face_data.user_id), 
                "total_images": len(face_data.img_url)
            }
        else:
            new_face_data = FaceData(user_id=uuid, img_url=url_list, created_at=datetime.now(), updated_at=datetime.now())
            db.add(new_face_data)
            await db.commit()
            await db.refresh(new_face_data)
            res = {
                "success": True, 
                "message": "Created new user with images", 
                "user_id": str(new_face_data.user_id), 
                "total_images": len(new_face_data.img_url)
            }
        
        return res
    except Exception as e:
        await db.rollback()
        return JSONResponse(
            status_code=500,
            content=error_response(
                error="Internal Server Error",
                message=str(e),
                status=500,
                path="/face/upload"
            )
        )