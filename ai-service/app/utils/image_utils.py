import io
import cloudinary
from fastapi.responses import StreamingResponse
import requests

def read_image_from_cloudinary(uid: str):
    public_id = f"users/face_{uid}"
    url = cloudinary.CloudinaryImage(public_id).build_url()
    r = requests.get(url, stream=True)
    r.raise_for_status()
    return r.content