FROM ghcr.io/ministryofjustice/hmpps-eclipse-temurin:25-jre-alpine

USER 2000

WORKDIR /app
COPY target/scala-2.13/ndelius2-*.jar /app/app.jar

EXPOSE 9000

CMD ["java", "-jar", "/app/app.jar"]
