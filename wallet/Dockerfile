# -------- STAGE 1: Build --------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY . .

# Fix permission issue
RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests


# -------- STAGE 2: Run --------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
