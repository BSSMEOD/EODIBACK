FROM gradle:8.14.3-jdk17 AS builder
WORKDIR /workspace

COPY gradle gradle
COPY gradlew settings.gradle build.gradle ./
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre
WORKDIR /eod

COPY --from=builder /workspace/build/libs/*.jar app.jar

RUN mkdir -p /eod/uploads /logs && \
    groupadd -r spring && \
    useradd -r -g spring spring && \
    chown -R spring:spring /eod /logs

USER spring:spring

ENTRYPOINT ["java", "-jar", "app.jar"]
