FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file
COPY target/paymentservice-*.jar app.jar

# Expose the port the app runs on
EXPOSE 8085

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"] 