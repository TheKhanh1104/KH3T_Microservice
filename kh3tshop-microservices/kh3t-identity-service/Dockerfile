FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# copy root pom and shared modules needed for build
COPY pom.xml .
COPY kh3t-common/pom.xml ./kh3t-common/pom.xml
COPY kh3t-common/src ./kh3t-common/src
COPY kh3t-identity-service/pom.xml ./kh3t-identity-service/pom.xml
COPY kh3t-identity-service/src ./kh3t-identity-service/src

RUN mvn -N -f pom.xml install && \
	mvn -f kh3t-common/pom.xml -DskipTests install && \
	mvn -f kh3t-identity-service/pom.xml -DskipTests clean package

FROM eclipse-temurin:21-jre

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /workspace/kh3t-identity-service/target/kh3t-identity-service-0.0.1-SNAPSHOT.jar /app/identity.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app/identity.jar"]
