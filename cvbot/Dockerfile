FROM eclipse-temurin:21-jdk-jammy

RUN apt-get update && apt-get install -y \
    nodejs \
    npm \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY package.json ./
RUN npm install --omit=dev

COPY convert.js ./
COPY pom.xml ./
COPY src ./src
COPY mvnw ./
COPY .mvn ./.mvn

RUN chmod +x mvnw && ./mvnw clean package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/cvbot-0.0.1-SNAPSHOT.jar"]