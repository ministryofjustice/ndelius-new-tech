FROM eclipse-temurin:21-jre-alpine

RUN addgroup --gid 2000 appgroup && \
    adduser --uid 2000 --system appuser --ingroup appgroup
USER 2000

WORKDIR /app
COPY target/scala-2.13/ndelius2-*.jar /app/app.jar

EXPOSE 9000

CMD ["java", "-jar", "/app/app.jar"]
