
# syntax=docker/dockerfile:1

# ---------- Build stage (Maven 3.8.8 + JDK 17) ----------
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# ---------- Runtime stage (JRE 17) ----------
FROM eclipse-temurin:17-jre-alpine
ENV APP_HOME=/opt/app
WORKDIR $APP_HOME

COPY --from=build /workspace/target/orders-inventory-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
