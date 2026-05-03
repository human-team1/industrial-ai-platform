import uuid

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse

from api.router import router
from application.exceptions import AppException
from config.settings import get_settings

settings = get_settings()

app = FastAPI(title=settings.app_name)
app.include_router(router, prefix="/ai/v1")


@app.middleware("http")
async def attach_request_id(request: Request, call_next):
    request_id = request.headers.get("X-Request-Id") or str(uuid.uuid4())
    request.state.request_id = request_id
    response = await call_next(request)
    response.headers["X-Request-Id"] = request_id
    return response


@app.exception_handler(RequestValidationError)
async def handle_validation_exception(request: Request, exc: RequestValidationError) -> JSONResponse:
    errors = exc.errors()
    status_code = 422
    if any(error.get("type") in {"missing", "json_invalid"} for error in errors):
        status_code = 400
    return JSONResponse(
        status_code=status_code,
        content={
            "type": "about:blank",
            "title": "Validation failed",
            "status": status_code,
            "detail": "요청 검증에 실패했습니다.",
            "instance": str(request.url.path),
            "errorCode": "VALIDATION_ERROR",
            "requestId": getattr(request.state, "request_id", None),
            "errors": [
                {
                    "loc": [str(value) for value in error.get("loc", [])],
                    "message": error.get("msg"),
                    "type": error.get("type"),
                }
                for error in errors
            ],
        },
    )


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
            "errorCode": exc.code,
            "requestId": getattr(request.state, "request_id", None),
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
            "requestId": getattr(request.state, "request_id", None),
        },
    )
