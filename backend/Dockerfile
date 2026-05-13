# Stage 1: compile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q
COPY src src
RUN ./mvnw package -DskipTests -q

# Stage 2: download only the Chromium browser binary (no system deps yet)
FROM eclipse-temurin:21-jdk-jammy AS browsers
WORKDIR /app
COPY --from=build /app/target/paytrack-1.0.jar app.jar
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
RUN jar xf app.jar BOOT-INF/lib && \
    java -cp "BOOT-INF/lib/*" com.microsoft.playwright.CLI install chromium

# Stage 3: lean runtime — JRE + Chromium system libs + browser + JAR
FROM eclipse-temurin:21-jre-jammy AS runtime
RUN apt-get update && apt-get install -y --no-install-recommends \
        libnss3 \
        libnspr4 \
        libatk1.0-0 \
        libatk-bridge2.0-0 \
        libcups2 \
        libdrm2 \
        libdbus-1-3 \
        libxkbcommon0 \
        libxcomposite1 \
        libxdamage1 \
        libxfixes3 \
        libxrandr2 \
        libgbm1 \
        libasound2 \
        libpango-1.0-0 \
        libcairo2 \
        libgtk-3-0 \
        libx11-xcb1 \
        fonts-liberation \
        ca-certificates \
    && rm -rf /var/lib/apt/lists/*
COPY --from=browsers /ms-playwright /ms-playwright
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1
WORKDIR /app
COPY --from=build /app/target/paytrack-1.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
