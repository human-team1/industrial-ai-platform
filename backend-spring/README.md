# Backend Spring

Spring Boot API 서버 전용 프로젝트입니다. SSR, JSP, WAR 구조를 사용하지 않고 persistence는 Spring Data JPA를 사용합니다.

## 기술 스택

- Java 17
- Spring Boot 2.7.18
- Gradle Wrapper 7.6.4
- Spring Data JPA
- MariaDB Java Client 2.7.5
- Spring Security
- OAuth2 Client
- Redis
- JJWT 0.11.5
- MinIO Java Client 8.5.12

## 환경변수

PowerShell:

```powershell
Copy-Item .env.example .env
```

주요 값:
- DB 접속 정보
- Redis 접속 정보
- MinIO endpoint/key/bucket
- Chroma host/port
- AI server base URL
- JWT secret/expire
- Google OAuth client 값

실제 비밀값은 커밋하지 않습니다.

## 실행

```powershell
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

## 테스트

```powershell
.\gradlew.bat test
```

## API 규칙

- API prefix는 `/api/v1`입니다.
- 성공 응답은 `ApiResponse` 구조를 사용합니다.
- 실패 응답은 Problem Details 스타일 JSON을 사용합니다.

## 주요 엔드포인트

- `GET /api/v1/health`
- `GET /api/v1/inspections/status`
- `GET /actuator/health`

## 패키지 구조

```text
com.example.factoryguard
  common/       # 공통 응답, 예외, 유틸
  config/       # security, persistence, web, client
  domain/       # 도메인 모델
  application/  # port, service, dto
  adapter/      # web, persistence, fastapi, minio, redis adapter
```

JPA Entity와 Spring Data Repository는 `adapter/out/persistence` 하위에 둡니다.
