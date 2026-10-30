# syntax=docker/dockerfile:1

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-25 AS build

WORKDIR /build

COPY pom.xml ./
COPY src ./src

RUN mvn -B -DskipTests package

# ---------- Runtime stage ----------
FROM eclipse-temurin:25-jre AS runtime

WORKDIR /app

RUN useradd --create-home --uid 10001 app

COPY --from=build /build/target/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
