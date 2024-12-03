# Usa l'immagine di base OpenJDK (Java 17 per progetti recenti di Spring Boot)
FROM openjdk:17-jdk-slim

# Imposta una directory di lavoro nell'immagine Docker
WORKDIR /app

# Copia il file JAR generato nel container
COPY target/telegram-webhook-0.0.1-SNAPSHOT.jar app.jar

# Espone la porta standard per Spring Boot (8080)
EXPOSE 8080

# Comando per eseguire l'applicazione Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
