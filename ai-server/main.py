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
    # 내부 추론 API(/ai/v1/internal/vision/infer-image 등) 기준 정책:
    #   - JSON 본체 자체가 깨진 경우(json_invalid)만 400으로 분리
    #   - 그 외 필수 필드 누락(missing) 및 형식 위반은 422 (RFC 9457 / Unprocessable Entity) 로 통일
    # 외부 공개 API에서 흔히 쓰는 "필수 파라미터 자체 누락 = 400" 정책과는 분리되어 있음.
    errors = exc.errors()
    status_code = 422
    if any(error.get("type") == "json_invalid" for error in errors):
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
