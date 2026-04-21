from fastapi import FastAPI, Request
from fastapi.responses import JSONResponse

from api.router import router
from application.exceptions import AppException
from config.settings import get_settings

settings = get_settings()

app = FastAPI(title=settings.app_name)
app.include_router(router, prefix="/ai/v1")


@app.exception_handler(AppException)
async def handle_app_exception(request: Request, exc: AppException) -> JSONResponse:
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "type": "about:blank",
            "title": exc.title,
            "status": exc.status_code,
            "detail": exc.detail,
            "instance": str(request.url.path),
            "code": exc.code,
        },
    )


@app.exception_handler(Exception)
async def handle_unexpected_exception(request: Request, exc: Exception) -> JSONResponse:
    return JSONResponse(
        status_code=500,
        content={
            "type": "about:blank",
            "title": "Internal Server Error",
            "status": 500,
            "detail": "Unexpected server error",
            "instance": str(request.url.path),
            "code": "COMMON-500",
        },
    )
