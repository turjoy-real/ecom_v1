FROM eclipse-temurin:17-jdk-focal
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src

ENV SERVER_PORT=8000
ENV DB_URL="mysql://host.docker.internal:3306/scalar_ecom_v1"
ENV DB_USER="turjoysaha"
ENV DB_PASS="Iam@007"
ENV EUREKA_SERVER="http://host.docker.internal:8761/eureka"

CMD ["./mvnw", "spring-boot:run"]

EXPOSE 8000