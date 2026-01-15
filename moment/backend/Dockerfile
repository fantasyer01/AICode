# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Configure Maven to use Alibaba Cloud mirror
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>\
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">\
<mirrors>\
<mirror>\
<id>aliyunmaven</id>\
<mirrorOf>*</mirrorOf>\
<name>Alibaba Cloud Maven</name>\
<url>https://maven.aliyun.com/repository/public</url>\
</mirror>\
</mirrors>\
</settings>' > /root/.m2/settings.xml

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
