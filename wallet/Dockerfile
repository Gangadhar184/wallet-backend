# -------- STAGE 1: Build --------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY . .

RUN ./mvnw clean package -DskipTests


# -------- STAGE 2: Run --------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the built jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

# Shell form so wildcard works
CMD java -jar app.jar
