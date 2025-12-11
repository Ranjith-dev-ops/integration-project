FROM eclipse-temurin:17-jdk

WORKDIR /app
COPY target/simple-java-app-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

