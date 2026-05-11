# Nginx TLS 인증서 (운영)

`docker-compose.prod.yml` 은 `.env.prod` 의 **`NGINX_SSL_DIR`**(호스트 디렉터리)를 컨테이너 `/etc/nginx/ssl` 로 **read-only bind** 합니다. Self-hosted 예: `C:/industrial-ai-runtime/nginx/ssl`. 로컬만 쓸 때는 `NGINX_SSL_DIR=./nginx/ssl` 로 이 폴더를 가리키면 됩니다.

nginx 가 기대하는 파일 이름:

| 파일 | 용도 |
|------|------|
| `fullchain.pem` | 서버 인증서(+ 중간 체인) |
| `privkey.pem` | 비밀키 |

Let's Encrypt 등을 쓸 때도 위 이름으로 배치하거나, 심볼릭 링크를 맞춥니다.

## 인증서가 없을 때 (로컬/프리플라이트용 self-signed 예시)

infra 디렉터리 기준 예시입니다. **경로에는 비밀을 커밋하지 마세요** (이 폴더는 `*.pem` 등 무시 처리).

### PowerShell (OpenSSL 필요)

```powershell
cd infra\nginx\ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 `
  -keyout privkey.pem -out fullchain.pem `
  -subj "/CN=localhost"
```

### 리눅스/macOS

```bash
cd infra/nginx/ssl
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout privkey.pem -out fullchain.pem \
  -subj "/CN=localhost"
```

브라우저는 self-signed 신뢰 경고가 나는 것이 정상입니다.

## HTTP-01 (Let's Encrypt) 등

외부에서 80 포트로 ACME 챌린지를 받아야 하는 경우 `docker-compose.prod.yml` 의 **80 포트 publish 주석**을 해제하고, 별도 `server { listen 80; ... }` 블록(예: `/.well-known/acme-challenge/`)을 추가하세요. 기본 prod 초안은 **443만 publish** 합니다.
