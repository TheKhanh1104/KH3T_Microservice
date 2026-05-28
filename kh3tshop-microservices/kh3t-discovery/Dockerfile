FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/target/kh3t-discovery-0.0.1-SNAPSHOT.jar /app/kh3t-discovery.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "/app/kh3t-discovery.jar"]