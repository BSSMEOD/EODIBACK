FROM gradle:8.14.3-jdk17 AS builder
WORKDIR /workspace

# 1) 빌드 스크립트만 먼저 복사 → 의존성 레이어 캐시 (소스 변경과 무관)
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 2) 소스 복사 후 빌드 → 소스 변경 시 컴파일만 재실행
COPY src src
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /eod

COPY --from=builder /workspace/build/libs/*.jar app.jar

RUN mkdir -p /eod/uploads /logs && \
    groupadd -r spring && \
    useradd -r -g spring spring && \
    chown -R spring:spring /eod /logs

USER spring:spring

ENTRYPOINT ["java", "-jar", "app.jar"]
