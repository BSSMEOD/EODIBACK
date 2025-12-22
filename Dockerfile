# Runtime stage with JRE (ARM64 compatible)
FROM eclipse-temurin:17-jre
WORKDIR /eod

# Copy the prebuilt jar from the workflow
COPY build/libs/*.jar app.jar

# Run as non-root user (Debian-based)
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

ENTRYPOINT ["java", "-jar", "app.jar"]
