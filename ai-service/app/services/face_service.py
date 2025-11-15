import httpx
import os
from app.config.settings import FACE_SIMILARITY_THRESHOLD

async def verify_from_service(server_image: bytes, client_image: bytes) -> float:
    async with httpx.AsyncClient() as client:
        res = await client.post(
            os.getenv('API_ENDPOINT') + "/verify",
            files= {
                "image1": ("image1.jpg", server_image, "image/jpeg"),
                "image2": ("image2.jpg", client_image, "image/jpeg")
            }
        )
    result = res.json()
    return result