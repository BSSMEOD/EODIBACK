# Runtime stage with JRE (ARM64 compatible)
FROM eclipse-temurin:17-jre
WORKDIR /eod

# Copy the prebuilt jar from the workflow
COPY build/libs/*.jar app.jar

# Create upload directory with proper permissions
RUN mkdir -p /eod/uploads && \
    groupadd -r spring && \
    useradd -r -g spring spring && \
    chown -R spring:spring /eod

# Run as non-root user (Debian-based)
USER spring:spring

ENTRYPOINT ["java", "-jar", "app.jar"]
