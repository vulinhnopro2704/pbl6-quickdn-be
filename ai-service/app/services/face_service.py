import httpx
import os
from typing import List

async def verify_from_service(server_images: List[bytes], client_image: bytes):
    files = []

    for idx, img_bytes in enumerate(server_images):
        files.append((
            "images1",
            (f"image1_{idx}.jpg", img_bytes, "image/jpeg")
        ))
    files.append((
        "image2",
        ("image2.jpg", client_image, "image/jpeg")
    ))

    async with httpx.AsyncClient() as client:
        res = await client.post(
            os.getenv("API_ENDPOINT") + "/verify",
            files=files
        )

    return res.json()
