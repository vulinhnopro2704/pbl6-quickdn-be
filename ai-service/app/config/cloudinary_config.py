import os
import cloudinary
from dotenv import load_dotenv

load_dotenv()

cloudname = os.getenv('CLOUD_NAME')
api_key = os.getenv('API_KEY')
api_secret = os.getenv('API_SECRET')

cloudinary.config(
    cloud_name = cloudname,
    api_key = api_key,
    api_secret = api_secret,
    secure = True
)
