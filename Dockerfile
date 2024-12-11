FROM openjdk:8-jre-slim

RUN apt-get update && \
    apt-get install -y curl jq && \
    rm -rf /var/lib/apt/lists/*

RUN addgroup --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000
USER 2000

WORKDIR /app
COPY target/scala-2.12/ndelius2-*.jar /app/app.jar

EXPOSE 9000

CMD ["java", "-jar", "/app/app.jar"]
