FROM gcr.io/distroless/java21

COPY build/libs/pam-internal-search-api-*-all.jar /app/app.jar
ENV JAVA_OPTS="-Xms768m -Xmx1024m"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"
EXPOSE 9027

ENTRYPOINT ["java", "-jar", "/app/app.jar"]