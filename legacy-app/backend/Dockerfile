# Backend VidaPlus (Java 21 + Spring Boot 3)
# Build multi-stage: compila com Maven/JDK 21, roda com JRE 21.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/vidaplus-1.0.0.jar app.jar
EXPOSE 8080
# V-13: roda em HTTP puro (sem TLS) - discutir no gate de DAST/infra
ENTRYPOINT ["java", "-jar", "app.jar"]
