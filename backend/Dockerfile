FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# 1. Install standard system maven directly
RUN apk add --no-cache maven

# 2. Only copy pom.xml first to download dependencies cleanly
COPY pom.xml .

# 3. Copy your backend source code
COPY src ./src

# 4. Compile and package the application using the installed system maven
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
WORKDIR /app

# Copies your final built artifact
COPY --from=builder /build/target/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]