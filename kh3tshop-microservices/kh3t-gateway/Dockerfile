FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY kh3t-common/pom.xml ./kh3t-common/pom.xml
COPY kh3t-common/src ./kh3t-common/src
COPY kh3t-gateway/pom.xml ./kh3t-gateway/pom.xml
COPY kh3t-gateway/src ./kh3t-gateway/src

RUN mvn -N -f pom.xml install && \
	mvn -f kh3t-common/pom.xml -DskipTests install && \
	mvn -f kh3t-gateway/pom.xml -DskipTests clean package

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/kh3t-gateway/target/kh3t-gateway-0.0.1-SNAPSHOT.jar /app/gateway.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/gateway.jar"]
