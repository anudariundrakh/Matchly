# Build the Spring Boot application
FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw -DskipTests package


# Run the application
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]