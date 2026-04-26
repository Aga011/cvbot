FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM node:20-bookworm-slim
RUN apt-get update && apt-get install -y \
    default-jre-headless \
    chromium \
    fonts-freefont-ttf \
    --no-install-recommends \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY --from=build /app/target/cvbot-0.0.1-SNAPSHOT.jar app.jar
COPY package.json convert.js ./
ENV PUPPETEER_SKIP_CHROMIUM_DOWNLOAD=true
RUN npm install

EXPOSE 8090
CMD ["java", "-jar", "app.jar"]
