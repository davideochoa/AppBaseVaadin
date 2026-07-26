FROM maven:3-eclipse-temurin-26 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# Dev mode on purpose (no VAADIN_PRODUCTIONMODE): pom.xml has no
# build-frontend/production profile yet, so this reuses the exact
# config already verified in Phase 4 (mvn spring-boot:run). First
# container start compiles the frontend via Vite and needs outbound
# internet access to fetch Node the first time.
# Not alpine here: Vaadin's dev-mode downloader fetches an official
# glibc-linked Node.js binary, which won't run on musl-based Alpine.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/app-vaadin-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
