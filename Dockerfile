FROM maven:3.9.4-amazoncorretto-21 AS build

WORKDIR /app

COPY java-no-spring/pom.xml .
COPY java-no-spring/src ./src

RUN mvn clean package

FROM amazoncorretto:21

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
COPY scripts/integration-test.sh /app/scripts/
RUN chmod +x /app/scripts/integration-test.sh

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]