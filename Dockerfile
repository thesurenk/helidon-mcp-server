# Multi-stage build for Helidon MCP Server
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/helidon-base-mcp-server-1.0.0.jar app.jar

# Expose port (if needed for future HTTP endpoints)
EXPOSE 8080

# Set the main class
ENTRYPOINT ["java", "-jar", "app.jar"]
