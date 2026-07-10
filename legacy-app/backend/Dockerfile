# Backend VidaPlus (legado)
# Build multi-stage: compila com Maven, roda com JRE 11.
FROM maven:3.8-openjdk-11 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline || true
COPY src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:11-jre
WORKDIR /app
COPY --from=build /app/target/vidaplus-1.0.0-LEGADO.jar app.jar
EXPOSE 8080
# V-13: roda em HTTP puro (sem TLS) - discutir no gate de DAST/infra
ENTRYPOINT ["java", "-jar", "app.jar"]
