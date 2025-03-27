FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

ENV SERVER_PORT=8761


CMD ["./mvnw", "spring-boot:run"]

EXPOSE 8761