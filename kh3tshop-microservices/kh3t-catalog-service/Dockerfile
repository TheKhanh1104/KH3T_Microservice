FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY kh3t-common/pom.xml ./kh3t-common/pom.xml
COPY kh3t-common/src ./kh3t-common/src
COPY kh3t-catalog-service/pom.xml ./kh3t-catalog-service/pom.xml
COPY kh3t-catalog-service/src ./kh3t-catalog-service/src

RUN mvn -f kh3t-common/pom.xml -DskipTests install && \
	mvn -f kh3t-catalog-service/pom.xml -DskipTests clean package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/kh3t-catalog-service/target/kh3t-catalog-service-0.0.1-SNAPSHOT.jar /app/catalog.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","/app/catalog.jar"]
