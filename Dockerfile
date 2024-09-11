# Use an official JDK image as the base
FROM openjdk:17-jdk-alpine

# Set a working directory
WORKDIR /app

# Copy the Spring Boot JAR file to the container
COPY target/event-backend-0.0.1-SNAPSHOT.jar /app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "/app.jar"]
