FROM ghcr.io/navikt/baseimages/temurin:17
COPY build/libs/pam-internal-search-api-*-all.jar /app/app.jar
EXPOSE 9027
ENV JAVA_OPTS="-Xms768m -Xmx1024m"
