# Stage 1: Build the JAR
FROM eclipse-temurin:25-jdk AS build

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Cache dependencies before copying source (faster rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Lean runtime image
FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/target/athlete-analytics-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]