# Build stage with dependency caching
FROM gradle:8.6-jdk17 AS build
WORKDIR /workspace

# Copy only dependency files first (for caching)
COPY build.gradle settings.gradle ./
COPY gradle gradle

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src src

# Build application
RUN gradle bootJar --no-daemon

# Runtime stage with JRE (ARM64 compatible)
FROM eclipse-temurin:17-jre
WORKDIR /eod

# Copy only the built jar
COPY --from=build /workspace/build/libs/*.jar app.jar

# Run as non-root user (Debian-based)
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

ENTRYPOINT ["java", "-jar", "app.jar"]
