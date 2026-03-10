# =====================
# Build stage
# =====================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml trước để cache dependency
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source
COPY src ./src

# Build jar
RUN mvn clean package -DskipTests

# =====================
# Run stage
# =====================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Ensure UTC timezone at OS and JVM level
ENV TZ=UTC

# Copy jar từ build stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "app.jar"]
