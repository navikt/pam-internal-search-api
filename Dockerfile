FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-25@sha256:bd2b5c78e153b13fab48d0c1159f2925545004545bf49f05b931a66e71ecdf17

COPY build/libs/pam-internal-search-api-*-all.jar /app/app.jar

ENV JDK_JAVA_OPTIONS="-XX:InitialRAMPercentage=25 -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"
EXPOSE 9027

CMD ["-jar", "/app/app.jar"]
