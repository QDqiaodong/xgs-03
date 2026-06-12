# syntax=docker/dockerfile:1.7
ARG DOCKER_REGISTRY=docker.m.daocloud.io/
FROM ${DOCKER_REGISTRY}library/maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY settings.xml /root/.m2/settings.xml

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -DskipTests dependency:resolve

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B package -DskipTests

ARG DOCKER_REGISTRY=docker.m.daocloud.io/
FROM ${DOCKER_REGISTRY}library/eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

RUN mkdir -p /app/uploads/plant-photos

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=8080"]
