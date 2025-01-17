FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy the Maven files first for better layer caching
COPY java-no-spring/pom.xml .
COPY java-no-spring/.mvn .mvn
COPY java-no-spring/mvnw .

# Copy source code
COPY java-no-spring/src src

# Create scripts directory and copy integration test
RUN mkdir -p /app/scripts
COPY scripts/integration-test.sh /app/scripts/
RUN chmod +x /app/scripts/integration-test.sh

# Build the application
RUN ./mvnw clean package

# Run the application
CMD ["java", "-jar", "target/java-no-spring-1.0-SNAPSHOT.jar"]
