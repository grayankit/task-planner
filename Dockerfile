# Build stage
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && ./gradlew :server:buildFatJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/server/build/libs/task-planner-server.jar ./app.jar

# Render provides PORT env var
EXPOSE 8080

# JVM tuning for 512MB RAM (Render free tier)
ENV JAVA_OPTS="-Xmx384m -Xms128m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
