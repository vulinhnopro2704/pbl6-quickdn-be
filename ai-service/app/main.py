from fastapi import FastAPI
from app.api.router import router as face_router
import app.config.cloudinary_config 

app = FastAPI()
app.include_router(face_router, prefix="/api")

@app.get("/")
def read_root():
    return {"message": "OK"}

@app.get("/health")
def health_check():
    return {"status": "healthy"}