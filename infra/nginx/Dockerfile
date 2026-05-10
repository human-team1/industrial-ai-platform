# curl 은 TLS 엔드포인트 healthcheck 용 (self-signed 는 -k)
FROM nginx:1.27-alpine
RUN apk add --no-cache curl
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD curl -kfsS https://127.0.0.1/ -o /dev/null || exit 1
