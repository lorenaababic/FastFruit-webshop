FROM maven:3.9.4-eclipse-temurin-21 AS build
WORKDIR /app
COPY webshop/pom.xml .
COPY webshop/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
