from fastapi import FastAPI
from app.api.router import router as face_router
import app.config.cloudinary_config 

app = FastAPI(
    title="AI Service API",
    description="Face recognition and image processing service",
    version="1.0.0",
    docs_url="/api/ai/docs",
    redoc_url="/api/ai/redoc",
    openapi_url="/api/ai/openapi.json"
)
app.include_router(face_router, prefix="/api/ai")

@app.get("/")
def read_root():
    return {"message": "OK"}

@app.get("/health")
def health_check():
    return {"status": "healthy"}