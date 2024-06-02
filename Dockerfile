# Start with a base image containing Java runtime
FROM openjdk:17-jdk-slim

# Add a volume pointing to /tmp
VOLUME /tmp

# Make port 9191 available to the world outside this container
EXPOSE 9191

# The application's jar file
ARG JAR_FILE=target/api-gateway-0.0.1-SNAPSHOT.jar

# Add the application's jar to the container
ADD ${JAR_FILE} api-gateway.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "/api-gateway.jar"]
