FROM navikt/java:12
COPY build/libs/pam-internal-search-api-*.jar /app/app.jar
EXPOSE 9027
