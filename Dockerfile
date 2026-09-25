# Stage 1: build the jar
FROM bellsoft/liberica-openjdk-debian:21 AS build
WORKDIR /app
COPY mvnw .
COPY .mvn ./.mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw package -DskipTests

# Stage 2: run it
FROM bellsoft/liberica-openjre-debian:21
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]