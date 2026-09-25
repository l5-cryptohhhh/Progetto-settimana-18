# Due fasi: nella prima si compila, nella seconda resta solo il jar.
# Cosi' nell'immagine finale non ci sono il JDK, Maven e i sorgenti.

FROM eclipse-temurin:25-jdk AS costruzione

WORKDIR /app

# Prima solo i file che descrivono le dipendenze: finche' il pom non cambia
# Docker riusa questo strato e non riscarica mezzo Maven Central a ogni deploy.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

COPY src/ src/
# I test girano in locale e in CI, non qui: servirebbero database e SMTP
# e allungherebbero ogni deploy senza dire niente di nuovo.
RUN ./mvnw -B clean package -DskipTests


FROM eclipse-temurin:25-jre

WORKDIR /app

# Non si gira da root: se qualcuno scappa dall'applicazione si ritrova
# con i permessi di un utente che non puo' fare niente.
RUN useradd --system --create-home salone
USER salone

COPY --from=costruzione /app/target/*.jar app.jar

# Render decide la porta con la variabile PORT, che application.yml gia' legge.
EXPOSE 8080

# Il piano gratuito di Render da' 512 MB. Senza questo flag la JVM si tiene
# un quarto scarso della memoria del contenitore e il resto resta inutilizzato:
# con 75% l'applicazione ci sta comoda e non viene uccisa per OOM.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
