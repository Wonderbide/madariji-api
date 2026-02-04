# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage - Alpine pour moins de mémoire
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Commande simple avec limite mémoire
CMD ["sh", "-c", "mkdir -p /data/uploads /data/covers /data/final-results && java -Xmx512m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -jar app.jar --server.port=${PORT:-8080} --server.address=0.0.0.0"]
