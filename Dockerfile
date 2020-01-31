FROM navikt/java:12
COPY target/pam-internal-search-api-*.jar /app/app.jar
EXPOSE 9027
