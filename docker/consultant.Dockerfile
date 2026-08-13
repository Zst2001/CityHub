FROM eclipse-temurin:17-jre
WORKDIR /app
COPY backend/consultant/target/cityhub-ai-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
