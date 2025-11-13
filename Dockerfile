FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/arkasha-1.0-SNAPSHOT-jar-with-dependencies.jar /app/target/arkasha-1.0-SNAPSHOT-jar-with-dependencies.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
