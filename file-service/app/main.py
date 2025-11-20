from fastapi import FastAPI
from .router import router as file_router
import app.config

app = FastAPI(
    title="File Service API",
    description="File upload, download, and management service with Cloudinary",
    version="1.0.0",
    docs_url="/api/file/docs",
    redoc_url="/api/file/redoc",
    openapi_url="/api/file/openapi.json"
)
app.include_router(file_router, prefix="/api/file")

@app.get("/api/file/")
def read_root():
    return {"message": "OK"}

@app.get("/api/file/health")
def health_check():
    return {"status": "healthy"}