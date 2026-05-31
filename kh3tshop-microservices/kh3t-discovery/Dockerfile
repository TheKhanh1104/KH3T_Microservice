FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# when build context is the microservices root, copy module sources explicitly
COPY pom.xml .
COPY kh3t-common/pom.xml ./kh3t-common/pom.xml
COPY kh3t-common/src ./kh3t-common/src
COPY kh3t-discovery/pom.xml ./kh3t-discovery/pom.xml
COPY kh3t-discovery/src ./kh3t-discovery/src

RUN mvn -N -f pom.xml install && \
	mvn -f kh3t-common/pom.xml -DskipTests install && \
	mvn -f kh3t-discovery/pom.xml -DskipTests clean package

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/kh3t-discovery/target/kh3t-discovery-0.0.1-SNAPSHOT.jar /app/kh3t-discovery.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "/app/kh3t-discovery.jar"]