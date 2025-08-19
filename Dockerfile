# ---------- BUILD STAGE ----------
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml ./

# Give execute permission to Maven wrapper
RUN chmod +x mvnw

# Download dependencies (offline)
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build the project and skip tests
RUN ./mvnw clean package -DskipTests

# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/target/*.jar ./app.jar

# Expose the port that matches application.properties
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]