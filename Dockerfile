FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-27@sha256:9f124e43e7d3c8605f42c6078f898421c9386087d87f1e1829a5bdc4e2a56bea

COPY build/libs/pam-internal-search-api-*-all.jar /app/app.jar

ENV JDK_JAVA_OPTIONS="-XX:InitialRAMPercentage=25 -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"
EXPOSE 9027

CMD ["-jar", "/app/app.jar"]
