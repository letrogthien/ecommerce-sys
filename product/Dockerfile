# Multi-stage build for Spring Boot Product Service
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install Node.js for search index setup (if needed)
RUN apk add --no-cache nodejs npm

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Create uploads directory
RUN mkdir -p uploads/products && \
    chown -R spring:spring uploads

# Copy built JAR from builder
COPY --from=builder --chown=spring:spring /app/target/*.jar app.jar

# Copy search setup scripts
COPY --chown=spring:spring setup-search-indexes.js sample-search-data.js ./

# Switch to non-root user
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# JVM optimizations
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
