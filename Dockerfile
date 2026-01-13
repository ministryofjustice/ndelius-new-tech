FROM eclipse-temurin:21-jre-alpine
LABEL maintainer="HMPPS Digital Studio <info@digital.justice.gov.uk>"

ARG BUILD_NUMBER
ENV BUILD_NUMBER ${BUILD_NUMBER:-1_0_0}
ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

RUN addgroup --gid 2000 appgroup && \
    adduser --uid 2000 --system appuser --ingroup appgroup
USER 2000

COPY --chown=appuser:appgroup ndelius2.jar /app/app.jar

EXPOSE 9000

CMD ["java", "-jar", "/app/app.jar"]
