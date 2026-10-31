FROM gradle:9.8.1-jdk21 AS build
WORKDIR /workspace
COPY --chown=gradle:gradle . .
RUN gradle --no-daemon bootJar

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 --create-home appuser
COPY --from=build /workspace/build/libs/nutrikitchen-0.0.1-SNAPSHOT.jar /app/app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
