# pam-search-api

## Read-only gateway/API mot ElasticSearch

### Kjøre fra localhost

#### IntelliJ

Lag maven configuration mot app-mappen, og sett 

command line:

    spring-boot:run -Dspring.profiles.active=dev 

Working directory: app/

Du får da instillingene fra application-dev.yml under app/src/test/resources, 
og kan gjøre tilpasninger der.

Alternativt, kjør klassen `DevApplication` som en Spring boot app fra IntelliJ.

#### Maven

Gå til app-mappen og kjør:

    mvn spring-boot:run -Dspring.profiles.active=dev
