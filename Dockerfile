FROM ghcr.io/ministryofjustice/hmpps-eclipse-temurin:25-jre-jammy

# New tech needs to write files to /app (e.g. logs and running PID), so change ownership to appuser
USER root
RUN chown -R appuser:appgroup /app
USER 2000

COPY --chown=appuser:appgroup target/scala-2.13/ndelius2-*.jar /app/app.jar

EXPOSE 9000

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
