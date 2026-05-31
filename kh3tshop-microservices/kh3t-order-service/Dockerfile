FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY kh3t-common/pom.xml ./kh3t-common/pom.xml
COPY kh3t-common/src ./kh3t-common/src
COPY kh3t-order-service/pom.xml ./kh3t-order-service/pom.xml
COPY kh3t-order-service/src ./kh3t-order-service/src

RUN mvn -N -f pom.xml install && \
	mvn -f kh3t-common/pom.xml -DskipTests install && \
	mvn -f kh3t-order-service/pom.xml -DskipTests clean package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/kh3t-order-service/target/kh3t-order-service-0.0.1-SNAPSHOT.jar /app/order.jar

EXPOSE 8083

ENTRYPOINT ["java","-jar","/app/order.jar"]
