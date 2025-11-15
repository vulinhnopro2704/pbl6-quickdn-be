# PBL-6-AI-SERVICE

Face recognition API service built with FastAPI.

## Requirements

- Python 3.8+
- pip
- Git

## Installation

### 1. Clone repository

```bash
git clone https://github.com/thanhdeptrai157/PBL-6-AI-SERVICE.git
cd PBL-6-AI-SERVICE
```

### 2. Create virtual environment

```bash
python -m venv venv

# On Windows
venv\Scripts\activate

# On macOS/Linux
source venv/bin/activate
```

### 3. Install dependencies

```bash
pip install -r requirements.txt
```

### 4. Environment configuration

Create `.env` file in the project root:

```env
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

API_ENDPOINT=your_api_endpoint
```

## Running the application

### 1. Start development server

```bash
uvicorn app.main:app --reload
```

Or with custom host and port:

```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

### 2. Access the application

- API Documentation (Swagger): http://localhost:8000/docs
- API Documentation (ReDoc): http://localhost:8000/redoc
- Health Check: http://localhost:8000/health
- Root endpoint: http://localhost:8000/

## API Endpoints

### Health Check
- `GET /health` - System health check
- `GET /` - Root endpoint

### Face Recognition API
- API endpoints are defined under `/api` prefix
- See details at: http://localhost:8000/docs

## Troubleshooting

### Common Issues

1. **ModuleNotFoundError**: Make sure virtual environment is activated and dependencies are installed
2. **Port already in use**: Change port using `--port` parameter
3. **Cloudinary errors**: Check configuration in `.env` file

### Debug mode

Run with detailed logging:

```bash
uvicorn app.main:app --reload --log-level debug
```