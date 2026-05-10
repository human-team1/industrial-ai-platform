import uuid
import logging
import sys
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

from api.router import router
from application.exceptions import AppException
from config.settings import get_settings

settings = get_settings()
log = logging.getLogger("uvicorn.error")


@asynccontextmanager
async def lifespan(app: FastAPI):
    log_runtime_cuda_status()
    if log.isEnabledFor(logging.DEBUG):
        for route in app.routes:
            methods = ",".join(sorted(getattr(route, "methods", []) or []))
            log.debug("runtime route registered: methods=%s path=%s", methods, getattr(route, "path", ""))
    yield


def log_runtime_cuda_status() -> None:
    try:
        import torch

        cuda_available = torch.cuda.is_available()
        device_name = torch.cuda.get_device_name(0) if cuda_available else "no cuda"
        torch_cuda = torch.version.cuda
    except Exception as exc:
        log.warning(
            "ai_server_runtime_cuda_status pythonExecutable=%s cudaAvailable=false torchCuda=unavailable deviceName=unavailable error=%s",
            sys.executable,
            exc.__class__.__name__,
        )
        return
    log.info(
        "ai_server_runtime_cuda_status pythonExecutable=%s cudaAvailable=%s torchCuda=%s deviceName=%s",
        sys.executable,
        cuda_available,
        torch_cuda,
        device_name,
    )


app = FastAPI(title=settings.app_name, lifespan=lifespan)
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
    status_code = 400 if any(error.get("type") == "json_invalid" for error in errors) else 422
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


@app.exception_handler(StarletteHTTPException)
async def handle_http_exception(request: Request, exc: StarletteHTTPException) -> JSONResponse:
    status_code = exc.status_code
    error_code = "ROUTE_NOT_FOUND" if status_code == 404 else "HTTP_ERROR"
    title = "Route not found" if status_code == 404 else "HTTP error"
    detail = f"No route for {request.method} {request.url.path}" if status_code == 404 else str(exc.detail)
    return JSONResponse(
        status_code=status_code,
        content={
            "type": "about:blank",
            "title": title,
            "status": status_code,
            "detail": detail,
            "instance": str(request.url.path),
            "errorCode": error_code,
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
