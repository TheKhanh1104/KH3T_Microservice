FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY kh3t-ai-service/pom.xml ./kh3t-ai-service/pom.xml
COPY kh3t-ai-service/src ./kh3t-ai-service/src

RUN mvn -N -f pom.xml install && \
	mvn -f kh3t-ai-service/pom.xml -DskipTests clean package

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/kh3t-ai-service/target/kh3t-ai-service-0.0.1-SNAPSHOT.jar /app/ai-service.jar

EXPOSE 8084

ENTRYPOINT ["java","-jar","/app/ai-service.jar"]
