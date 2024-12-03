# Usa un'immagine di base con Maven e JDK preinstallati
FROM maven:3.9.4-eclipse-temurin-17 AS build

# Imposta la directory di lavoro per Maven
WORKDIR /app

# Copia i file di configurazione Maven
COPY pom.xml .

# Scarica le dipendenze Maven (per sfruttare la cache Docker)
RUN mvn dependency:go-offline -B

# Copia il resto del codice sorgente nel container
COPY src ./src

# Compila il progetto e genera il file JAR
RUN mvn clean package -DskipTests

# Fase 2: Runtime
FROM openjdk:17-jdk-slim

# Imposta la directory di lavoro nel container
WORKDIR /app

# Copia il file JAR dalla fase di build
COPY --from=build /app/target/telegram-webhook-0.0.1-SNAPSHOT.jar app.jar

# Espone la porta 8080 per Spring Boot
EXPOSE 8080

# Comando di avvio
ENTRYPOINT ["java", "-jar", "app.jar"]
