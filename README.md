# pam-internal-search-api

Read-only gateway/API mot OpenSearch for interne stillingsannonser.

## Kjøre lokalt

Krever en OpenSearch-instans. Sett miljøvariabler og kjør jar-en:

```bash
./gradlew build
```
```bash
OPEN_SEARCH_URI=http://localhost:9200 \
OPEN_SEARCH_USERNAME=admin \
OPEN_SEARCH_PASSWORD=admin \
java -jar build/libs/pam-internal-search-api-0.1-all.jar
```

## Tester
```bash
./gradlew test
```
