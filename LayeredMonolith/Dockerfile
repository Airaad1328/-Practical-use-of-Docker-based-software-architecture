FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app
COPY src/ src
COPY pom.xml ./
RUN mvn package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/LayeredMonolith-0.0.1-SNAPSHOT.jar .
CMD ["java", "-jar", "LayeredMonolith-0.0.1-SNAPSHOT.jar"]