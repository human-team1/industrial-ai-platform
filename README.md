# industrial-ai-platform

Windows 10/11 신규 개발자 PC에서 `industrial-ai-platform` 개발환경을 구성하기 위한 루트 가이드입니다. 아무것도 설치되어 있지 않은 상태를 기준으로 작성했습니다.

## 1. 프로젝트 개요

`industrial-ai-platform`은 산업 현장 이상 탐지 시스템의 초기 개발환경과 모노레포 골격입니다. 현재 단계는 비즈니스 로직보다 로컬 개발환경, 서비스 분리, 설정 파일, 환경변수 템플릿, 실행 가능성 확보에 집중합니다.

전체 구성요소:

| 디렉터리 | 역할 |
| --- | --- |
| `infra/` | Docker Compose로 실행하는 MariaDB, Redis, MinIO, Chroma |
| `backend-spring/` | Spring Boot API 서버, `/api/v1` prefix |
| `frontend/` | React + Vite + TypeScript 클라이언트 |
| `ai-server/` | FastAPI 기반 이상 탐지/RAG 서버, `/ai/v1` prefix |
| `docs/` | 로컬 실행 구조와 개발환경 문서 |

Docker는 현재 `infra`에만 사용합니다. `backend-spring`, `frontend`, `ai-server`는 디버깅, 빠른 수정, IDE 연동을 쉽게 하기 위해 로컬 런타임으로 실행합니다. 추후 배포 환경에서는 애플리케이션 서비스도 컨테이너화되거나 운영 인프라 구성이 달라질 수 있습니다.

## 2. 사전 준비물

Windows 10/11 PC에 다음 도구를 설치합니다.

| 도구 | 고정/권장 버전 | 용도 |
| --- | --- | --- |
| Git | 최신 안정 버전 | 저장소 clone, 버전 관리 |
| Visual Studio Code | 최신 안정 버전 | 공통 IDE |
| WSL2 | Windows 기능 | Docker Desktop의 Linux 기반 실행 환경 |
| Docker Desktop | 최신 안정 버전 | 로컬 인프라 실행 |
| JDK | 17 | Spring Boot 실행 |
| Python | 3.10.6 | FastAPI AI 서버 실행 |
| Node.js | 20.18.0 | Frontend 실행 |
| npm | 10.8.2 | Node.js에 포함, frontend 패키지 관리 |

Python 3.10.6과 Node.js 20.18.0은 프로젝트 고정 버전입니다. Python 3.10.6이 최신 버전은 아닐 수 있지만, 현재 AI 패키지 호환성을 위해 고정합니다.

## 3. 권장 설치 순서

1. Git 설치
2. VS Code 설치
3. WSL2 설치
4. Docker Desktop 설치
5. JDK 17 설치
6. Python 3.10.6 설치
7. Node.js 20.18.0 설치
8. VS Code 확장 설치
9. 프로젝트 clone
10. 환경변수 파일 생성
11. `infra` 실행
12. `backend-spring`, `ai-server`, `frontend` 실행

## 4. 설치 가이드

### A. Git

무엇을 설치하나요:
- Git for Windows

왜 필요한가요:
- GitHub/GitLab 저장소 clone, branch 관리, commit에 필요합니다.

설치 방식:
- Git for Windows 설치 파일을 사용합니다.
- 설치 중 PATH 옵션은 기본값을 사용해도 대부분 PowerShell에서 `git` 명령을 사용할 수 있습니다.

확인:

```powershell
git --version
```

주의사항:
- 명령어가 인식되지 않으면 PowerShell을 새로 열고 다시 확인합니다.
- 그래도 안 되면 Git 설치 경로의 `cmd` 또는 `bin`이 PATH에 있는지 확인합니다.

### B. Visual Studio Code

무엇을 설치하나요:
- VS Code Windows installer

왜 필요한가요:
- Java, Python, TypeScript, Docker 작업을 한 IDE에서 처리하기 위해 사용합니다.

설치 방식:
- Windows installer로 설치합니다.
- 설치 옵션에서 "Add to PATH"와 "Open with Code" 항목을 선택하면 편합니다.

사용:

```powershell
code .
```

주의사항:
- 프로젝트 루트인 `industrial-ai-platform` 폴더를 VS Code로 여세요.
- 터미널은 VS Code 통합 터미널의 PowerShell을 기준으로 사용합니다.

### C. WSL2

무엇을 설치하나요:
- Windows Subsystem for Linux 2

왜 필요한가요:
- Docker Desktop이 Linux 컨테이너를 안정적으로 실행하는 기반입니다.

설치 방식:
- 관리자 권한 PowerShell에서 실행합니다.

```powershell
wsl --install
```

설치 후 재부팅이 필요할 수 있습니다.

확인:

```powershell
wsl -l -v
```

주의사항:
- Ubuntu 같은 기본 배포판이 설치되어 있고 VERSION이 `2`인지 확인합니다.
- VERSION이 1이면 WSL2로 전환해야 합니다.

### D. Docker Desktop

무엇을 설치하나요:
- Docker Desktop for Windows

왜 필요한가요:
- MariaDB, Redis, MinIO, Chroma를 로컬 인프라로 실행합니다.

설치 방식:
- Docker Desktop installer를 사용합니다.
- 설치 후 Docker Desktop을 실행합니다.
- Settings > Resources > WSL Integration에서 사용하는 WSL 배포판 연동을 켭니다.

확인:

```powershell
docker --version
docker compose version
```

주의사항:
- Docker Desktop이 켜져 있어야 `docker compose`가 동작합니다.
- `Cannot connect to the Docker daemon` 오류가 나오면 Docker Desktop 실행 상태를 먼저 확인합니다.

### E. JDK 17

무엇을 설치하나요:
- JDK 17

왜 필요한가요:
- Spring Boot backend를 실행하고 Gradle Wrapper를 사용하기 위해 필요합니다.

설치 방식:
- Windows용 JDK 17 installer를 사용합니다.
- 설치 후 `JAVA_HOME`과 PATH를 확인합니다.

시스템 변수 예시:

```text
JAVA_HOME=C:\Program Files\Java\jdk-17
Path=%JAVA_HOME%\bin
```

확인:

```powershell
java -version
javac -version
```

주의사항:
- `java`는 되는데 `javac`가 안 되면 JRE가 아니라 JDK가 설치되었는지 확인합니다.
- 환경변수 변경 후 PowerShell을 새로 열어야 반영됩니다.

### F. Python 3.10.6

무엇을 설치하나요:
- Python 3.10.6 Windows installer

왜 필요한가요:
- FastAPI AI 서버와 RAG/문서 처리 패키지를 실행합니다.

설치 방식:
- Python 3.10.6 installer를 사용합니다.
- 설치 첫 화면에서 "Add Python to PATH"를 선택합니다.

확인:

```powershell
python --version
pip --version
```

주의사항:
- 버전이 `Python 3.10.6`인지 확인합니다.
- AI 서버는 프로젝트 내부 `ai-server/.venv` 가상환경을 사용합니다.

### G. Node.js 20.18.0

무엇을 설치하나요:
- Node.js 20.18.0

왜 필요한가요:
- React + Vite frontend 개발 서버와 빌드에 필요합니다.

설치 방식:
- Node.js Windows installer를 사용합니다.
- npm은 Node.js에 포함됩니다.

확인:

```powershell
node -v
npm -v
```

기대 버전:

```text
node: v20.18.0
npm: 10.8.2
```

주의사항:
- `npm`이 인식되지 않으면 PowerShell을 새로 열고, Node.js 설치 경로가 PATH에 있는지 확인합니다.

## 5. VS Code 권장 확장

| 확장 | 용도 |
| --- | --- |
| Extension Pack for Java | Java 개발, Gradle, 테스트 지원 |
| Spring Boot Extension Pack | Spring Boot 실행/설정 지원 |
| Python | Python 인터프리터, 테스트, 디버깅 |
| Pylance | Python 타입 분석과 자동완성 |
| ESLint | Frontend lint 표시 |
| Prettier | Frontend formatting |
| Docker | Compose, container, image 확인 |
| Remote - WSL | WSL 환경 접근과 Docker 연동 작업 보조 |

## 6. 시스템 변수 / PATH 가이드

확인 명령:

```powershell
git --version
java -version
javac -version
python --version
pip --version
node -v
npm -v
docker --version
docker compose version
```

점검 포인트:
- Java는 `JAVA_HOME`이 JDK 17 경로를 가리켜야 합니다.
- Java `bin` 경로가 PATH에 있어야 `java`, `javac`가 동작합니다.
- Python, Node.js, Git은 보통 installer가 PATH를 자동 등록합니다.
- 명령어가 인식되지 않으면 PowerShell을 새로 열고 다시 확인합니다.
- Docker Desktop은 일반적으로 수동 PATH 편집보다 앱 실행 상태와 WSL Integration 확인이 중요합니다.

## 7. 프로젝트 clone 및 초기 세팅

저장소를 clone합니다.

```powershell
git clone <REPOSITORY_URL>
cd industrial-ai-platform
git branch
```

프로젝트 루트 구조:

```text
industrial-ai-platform/
  infra/
  backend-spring/
  frontend/
  ai-server/
  docs/
```

각 서비스 위치:
- `infra`: 로컬 DB/cache/storage/vector DB
- `backend-spring`: Java/Spring API
- `frontend`: React client
- `ai-server`: Python/FastAPI AI API

## 8. 환경변수 파일 생성 방법

PowerShell 기준:

```powershell
Copy-Item infra\.env.example infra\.env
Copy-Item frontend\.env.example frontend\.env
Copy-Item backend-spring\.env.example backend-spring\.env
Copy-Item ai-server\.env.example ai-server\.env
```

채워야 할 값:
- MariaDB root/user password
- MinIO access key, secret key, bucket
- Backend `JWT_SECRET`
- Google OAuth `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`
- API base URL
- AI model, embedding model, LLM model 이름

실제 비밀값은 Git에 커밋하지 않습니다.

## 9. 인프라 실행 가이드

```powershell
cd infra
docker compose --env-file .env up -d
docker ps
```

중지:

```powershell
docker compose down
```

포트:

| 서비스 | 포트 |
| --- | --- |
| MariaDB | `localhost:3307` |
| Redis | `localhost:6379` |
| MinIO API | `http://localhost:9000` |
| MinIO Console | `http://localhost:9001` |
| Chroma | `http://localhost:8000` |

## 10. Backend 실행 가이드

Java 확인:

```powershell
java -version
javac -version
```

실행:

```powershell
cd backend-spring
.\gradlew.bat bootRun --args="--spring.profiles.active=local"
```

테스트:

```powershell
.\gradlew.bat test
```

확인:
- `http://localhost:8080/api/v1/health`
- `http://localhost:8080/actuator/health`

필요 환경변수:
- DB, Redis, MinIO, Chroma, AI server URL
- JWT secret/expire
- Google OAuth client 값

## 11. AI Server 실행 가이드

```powershell
cd ai-server
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn main:app --reload --host 0.0.0.0 --port 8001
```

실행 확인:
- `http://localhost:8001/docs`
- `http://localhost:8001/ai/v1/health`

테스트:

```powershell
pytest
```

PowerShell 실행 정책 때문에 activate가 막히면 현재 사용자 범위로 완화합니다.

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## 12. Frontend 실행 가이드

```powershell
cd frontend
npm install
npm run dev
```

확인:
- `http://localhost:5173`

빌드:

```powershell
npm run build
```

Lint:

```powershell
npm run lint
```

참고:
- `npm audit` 경고가 나올 수 있습니다.
- 현재 단계에서는 강제 업그레이드보다 프로젝트 고정 버전과 호환성을 우선합니다.

## 13. 설치 및 실행 확인 체크리스트

- [ ] `git --version` 확인
- [ ] `java -version` 확인
- [ ] `javac -version` 확인
- [ ] `python --version` 확인
- [ ] `pip --version` 확인
- [ ] `node -v` 확인
- [ ] `npm -v` 확인
- [ ] `docker compose version` 확인
- [ ] VS Code 권장 확장 설치
- [ ] `.env` 파일 생성
- [ ] `infra` 기동 확인
- [ ] `backend-spring` 실행 확인
- [ ] `ai-server` 실행 확인
- [ ] `frontend` 실행 확인

## 14. 자주 겪는 문제 해결

### npm 명령어 인식 안 됨
- PowerShell을 새로 엽니다.
- `node -v`, `npm -v`를 확인합니다.
- Node.js 설치 경로가 PATH에 있는지 확인합니다.

### java 명령어 인식 안 됨
- `JAVA_HOME`이 JDK 17 경로인지 확인합니다.
- PATH에 `%JAVA_HOME%\bin`이 있는지 확인합니다.
- PowerShell을 새로 열고 다시 확인합니다.

### docker daemon 연결 실패
- Docker Desktop이 실행 중인지 확인합니다.
- Docker Desktop 초기화가 끝날 때까지 기다립니다.
- WSL Integration이 켜져 있는지 확인합니다.

### .env 변수 미인식
- `.env.example`이 아니라 `.env` 파일을 만들었는지 확인합니다.
- 현재 터미널의 작업 디렉터리가 각 서비스 폴더인지 확인합니다.
- backend는 Spring `application.yml`의 기본값도 함께 확인합니다.

### WSL/Docker 연동 안 됨
- `wsl -l -v`로 WSL2 배포판 상태를 확인합니다.
- Docker Desktop Settings > Resources > WSL Integration을 확인합니다.

### PowerShell 실행 정책으로 venv activate 실패

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

명령 실행 후 PowerShell을 새로 열고 다시 시도합니다.

### 포트 충돌
- MariaDB 기본 포트 `3306`을 피하기 위해 compose는 `3307:3306`을 사용합니다.
- 이미 `3307`, `6379`, `9000`, `9001`, `8000`, `8001`, `8080`, `5173`을 사용하는 프로세스가 있으면 종료하거나 포트를 조정해야 합니다.
