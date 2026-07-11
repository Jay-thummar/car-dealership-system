# Stage 1: Build the Spring Boot application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY backend /app/backend
WORKDIR /app/backend
RUN mvn clean package -DskipTests

# Stage 2: Run the Spring Boot application
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/backend/target/backend-0.0.1-SNAPSHOT.jar /app/backend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/backend.jar"]
