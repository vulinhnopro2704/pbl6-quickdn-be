import cloudinary
import cloudinary.uploader
from fastapi import APIRouter, Form, UploadFile, File, status
from fastapi import HTTPException
from app.services.face_service import verify_from_service
from app.utils.image_utils import read_image_from_cloudinary

router = APIRouter(prefix="/face", tags=["Face Recognition"])

@router.post("/upload", status_code=status.HTTP_201_CREATED)
async def upload_image(uid: str = Form(...), file: UploadFile = File(...)):
    try:
        result = cloudinary.uploader.upload(
            file.file,
            folder="users",
            public_id=f"face_{uid}",
            overwrite=True,
            resource_type="image"
        )
        return {
            "message": "Upload successful",
            "user_id": uid,
            "url": result["secure_url"],
            "public_id": result["public_id"]
        }

    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Upload failed: {str(e)}"
        )
        
@router.post("/verify", status_code=status.HTTP_200_OK)
async def verify_face(uid: str = Form(...), file: UploadFile = File(...)):
    try:
        server_image = read_image_from_cloudinary(uid)
        if (server_image is None):
            raise HTTPException(status_code=404, detail="User image not found")
        client_image = file.file.read()
        res = await verify_from_service(server_image, client_image)
        return res
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))