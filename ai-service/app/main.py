from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.api.router import router as face_router
from app.config.database import async_engine, Base

@asynccontextmanager
async def lifespan(app: FastAPI):
    try:
        async with async_engine.connect() as conn:
            print("Database connected successfully!")
    except Exception as e:
        print(f"Database connection failed: {e}")
    yield
    await async_engine.dispose() 


app = FastAPI(
    title="AI Service API",
    description="Face recognition and image processing service",
    version="1.0.0",
    docs_url="/api/ai/docs",
    redoc_url="/api/ai/redoc",
    openapi_url="/api/ai/openapi.json",
    lifespan=lifespan,
)

app.include_router(face_router, prefix="/api/ai")

@app.get("/")
def read_root():
    return {"message": "OK"}

@app.get("/health")
def health_check():
    return {"status": "healthy"}