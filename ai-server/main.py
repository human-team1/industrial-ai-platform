from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from api.router import router
from application.exceptions import AppException
from config.settings import get_settings

settings = get_settings()

app = FastAPI(title=settings.app_name)
app.include_router(router, prefix="/ai/v1")


@app.exception_handler(AppException)
async def handle_app_exception(request: Request, exc: AppException) -> JSONResponse:
    request_id = request.headers.get("X-Request-Id")
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "type": "about:blank",
            "title": exc.title,
            "status": exc.status_code,
            "detail": exc.detail,
            "instance": str(request.url.path),
            "errorCode": exc.code,
            "requestId": request_id,
        },
    )


@app.exception_handler(RequestValidationError)
async def handle_validation_exception(request: Request, exc: RequestValidationError) -> JSONResponse:
    has_missing = any(error.get("type") == "missing" for error in exc.errors())
    status_code = 400 if has_missing else 422
    title = "Bad Request" if has_missing else "Unprocessable Content"
    return JSONResponse(
        status_code=status_code,
        content={
            "type": "about:blank",
            "title": title,
            "status": status_code,
            "detail": "요청 본문을 확인할 수 없습니다." if has_missing else "요청 값 검증에 실패했습니다.",
            "instance": str(request.url.path),
            "errorCode": "COMMON-400" if has_missing else "COMMON-422",
            "requestId": request.headers.get("X-Request-Id"),
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
            "errorCode": "COMMON-500",
            "requestId": request.headers.get("X-Request-Id"),
        },
    )
