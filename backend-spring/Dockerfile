# 레포에 gradle-wrapper.jar 가 없어도 동작하도록 Gradle 배포판 이미지로 빌드합니다.
FROM gradle:7.6.4-jdk17-jammy AS builder
WORKDIR /workspace

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# curl: Compose healthcheck 에서 `GET /api/v1/health` 확인용 (이미지 단독 실행 시에는 건드리지 않음).
#
# TODO(gradle-wrapper.jar): 선택적으로 레포에 wrapper 가 있으면 동일 Dockerfile 에서 `./gradlew` 경로도 고려 가능.
# 현재는 builder 가 gradle 배포판 이미지(gradle:7.6.4-jdk17-jammy) 로 bootJar 를 실행함.
