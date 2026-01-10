# Backend Dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build application
RUN ./mvnw package -DskipTests

# Run application
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/transaction-training-1.0.0.jar"]
