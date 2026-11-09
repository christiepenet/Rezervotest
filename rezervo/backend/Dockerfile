# ---- Etapa 1: compilare (are Maven + JDK) ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- Etapa 2: rulare (doar Java runtime, imagine mica) ----
FROM eclipse-temurin:21-jre
WORKDIR /app
# Rulam ca utilizator obisnuit, nu ca root
RUN useradd --system --uid 10001 appuser
COPY --from=build /app/target/rezervo-backend.jar app.jar
USER appuser
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
