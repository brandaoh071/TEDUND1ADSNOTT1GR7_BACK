# syntax=docker/dockerfile:1

# ---- build: Maven 3.9 + JDK 21 (mesma versão do pom: java.version=21) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Dependências primeiro, para aproveitar o cache quando só o código muda
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -DskipTests package \
    && cp target/*.jar /build/app.jar

# ---- runtime: só o JRE 21 ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S ted && adduser -S ted -G ted
COPY --from=build /build/app.jar app.jar
USER ted

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
