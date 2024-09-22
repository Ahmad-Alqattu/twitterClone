FROM openjdk:17-jdk-slim
COPY target/twitterClone-1.0.jar app.jar
EXPOSE 7000
ENTRYPOINT ["java", "-jar", "app.jar"]
