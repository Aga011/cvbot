FROM node:20-slim AS node-base

FROM eclipse-temurin:21-jdk-jammy

COPY --from=node-base /usr/local/bin/node /usr/local/bin/node
COPY --from=node-base /usr/local/lib/node_modules /usr/local/lib/node_modules
RUN ln -s /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm

WORKDIR /app

COPY package.json ./
RUN npm install

COPY . .
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/cvbot-0.0.1-SNAPSHOT.jar"]