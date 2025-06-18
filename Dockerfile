# Bruk Java 17 som base image (du kan bytte til 21 om nødvendig)
FROM eclipse-temurin:17-jdk-alpine

# Sett arbeidskatalogen
WORKDIR /app

# Kopier Maven wrapper og pom.xml
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# Last ned dependencies (hurtig-cache)
RUN ./mvnw dependency:go-offline

# Kopier resten av prosjektet
COPY . .

# Bygg prosjektet
RUN ./mvnw clean install -DskipTests

# Kjør appen
CMD ["java", "-jar", "target/demo-0.0.1-SNAPSHOT.jar"]
