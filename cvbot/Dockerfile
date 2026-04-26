FROM ghcr.io/puppeteer/puppeteer:21.0.0 AS node-base

FROM eclipse-temurin:21-jdk-jammy

COPY --from=node-base /usr/local/bin/node /usr/local/bin/node
COPY --from=node-base /usr/local/lib/node_modules /usr/local/lib/node_modules
COPY --from=node-base /home/pptruser/.cache /root/.cache
RUN ln -s /usr/local/lib/node_modules/npm/bin/npm-cli.js /usr/local/bin/npm
RUN ln -s /usr/local/lib/node_modules/puppeteer /app/node_modules/puppeteer

WORKDIR /app

RUN mkdir -p node_modules
RUN ln -sf /usr/local/lib/node_modules/puppeteer node_modules/puppeteer

COPY convert.js ./
COPY pom.xml ./
COPY src ./src
COPY mvnw ./
COPY .mvn ./.mvn

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/cvbot-0.0.1-SNAPSHOT.jar"]