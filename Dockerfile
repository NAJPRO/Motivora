# ---- Build stage -------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Dependencies are resolved from the POM alone, so this layer is reused whenever
# only sources change.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# Never run the API as root.
RUN addgroup -S motivora && adduser -S motivora -G motivora

COPY --from=build /build/target/*.jar app.jar

# Uploaded images live on a volume: the container filesystem is disposable.
RUN mkdir -p /app/uploads && chown -R motivora:motivora /app
VOLUME ["/app/uploads"]

USER motivora
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
