FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/oauthserver-*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"] 