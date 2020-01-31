PAM ElasticSearch API for spørringer
====================================

Dette dokumentet er ment som en hjelp til å komme i gang med å lage 
ES-spørringer og vil inneholde konkrete og domenespesifikke eksempler for 
annonser og firmaer og feltene man opererer med. Etterhvert vil det også 
inneholde anbefalte standardinnstillinger for forskjellige typer søk, også i 
forhold til relevanstuning. Siden vi eksponerer ElasticSearch REST-søke-API 
direkte vil det også være relevant å lese generell dokumentasjon om ES for 
front-utviklere.

Dette er API for å jobbe med interne Stillinger, det vil si de som ikke er utlyst enda.
Se på pam-search-api for informasjon rundt publiserte stillinger, og firmainformasjon

Indekser og dokumenttyper
-------------------------

Det er en dokumenttyper som er søkbare i ElasticSearch gjennom dette api'et og det finnes i indeksen `internalad```

Generelt for å søke i annonser benyttes endepunkt:

http://localhost:9000/internalad/_search

Søk mot ES fra fronter og andre komponeter skal gå gjennom tjenesten
pam-search-api, som videresender direkte til ES-cluster. For lokal
utvikling kjører man typisk opp denne tjenesten på localhost (default
port 9000), og den er som standard satt opp til å søke mot ES i
nais test.


Nyttige generelle opsjoner for søk mot ES
-----------------------------------------

### Source-filtrering

ES sender data fra dokumentene i `_source`-feltet innenfor et hit.
Men man trenger ofte ikke hele dokumentet og kildedokumenter kan med
fordel filtreres ved bruk av `_source`-filtering i query request body,
se:

https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-source-filtering.html

### Response-filtering

Man kan generelt filtrere hele JSON-responsen fra ES for å begrense
til relevante/ønskede data. Det må gjøres med `filter_path`
URL-parameter.

Foreløpig er ikke bruk av denne parameteren støttet av pam-search-api,
men dokumentasjon finnes her:

https://www.elastic.co/guide/en/elasticsearch/reference/current/common-options.html#common-options-response-filtering

### Begrense størrelse + paginering med `size` og `from`

For å paginere i søkeresultat er det enklest å benytte parameter
`size` og `from` i query request body, f.eks. for resultat 11–20:

```json
{ "query": { "match_all": {} },
  "size": 10,
  "from": 10 }
```

Merk at `from` er 0-basert, første søkeresultat har offset 0.

Dokumentasjon:

https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-from-size.html


Fritekstsøk annonser
--------------------

Generell spørring for å søke fritekst i tittel, innhold og kategori. Alle 
forekomster av "<INPUT>" erstattes med input fra bruker. Søket filtreres på 
annonsestatus `ACTIVE`. Rekkefølge skal være basert på score/relevanse.

Endepunkt: `/ad/_search`

```json
{
  "query": {
    "bool": {
      "must": {
        "multi_match": {
          "query": "<INPUT>",
          "fields": [
            "title_no^2",
            "category_no^2",
            "searchtags_no^0.3",
            "adtext_no^0.2",
            "employer.name^0.1",
            "employerdescription_no^0.1"
          ],
          "tie_breaker": 0.1,
          "zero_terms_query": "all"
        }
      },
      "should": {
        "match_phrase": {
          "title": {
            "query": "<INPUT>",
            "slop": 3
          }
        }
      },
      "filter": {
        "term": {
          "status": "ACTIVE"
        }
      }
    }
  },
  "_source": [
    "title",
    "updated",
    "properties.adtext",
    "categoryList.name"
  ]
}
```

Samme som over, men med deboost av eldre aktive annonser (altså boost til nyere annonser).

TODO denne trenger tuning og avklaring av datofeltet `updated`, som ikke ser ut 
til å korrespondere med datoer i annonsemottak for pam-ad.

```json

{
  "query": {
    "function_score": {
      "query": {
        "bool": {
          "must": {
            "multi_match": {
              "query": "<INPUT>",
              "fields": [
                "title_no^2",
                "category_no^2",
                "adtext_no^0.5"
              ],
              "tie_breaker": 0.1
            }
          },
          "should": {
            "match_phrase": {
              "title": {
                "query": "<INPUT>",
                "slop": 3
              }
            }
          },
          "filter": {
            "term": {
              "status": "ACTIVE"
            }
          }
        }
      },
      "functions": [
        {
          "gauss": {
            "updated": {
              "scale": "1m"
            }
          }
        }
      ]
    }
  },
  "_source": [
    "title",
    "updated",
    "properties.adtext",
    "categoryList.name"
  ]
}

```

Fasettering (aggregation i ES)
------------------------------

### Generelt

ElasticSearch bruker order "aggregations" for tradisjonelle søkefasetter.

Aggregeringer forholder seg til gjeldende søk (`{ "query": {...}
...}`) og viser bare bucket-tall innenfor treffmengden. Om man
f.eks. hadde et query som begrenset til annonser som inneholder
"renholder" i tittel, så ville aggregation-tallene kun telt innenfor
de annonsene. For å se det globale bildet for en fasett kan en
`match_all` query benyttes.

For felter som ikke er påkrevde, og følgelig kan mangle i annonser, så
trenger man en "missing-aggregering" som teller alle dokumenter som
/ikke/ har noen verdi i feltet, innenfor treffmengden.

Man kan alltid sende med flere aggregeringer i samme søke-request, og responsen 
vil inneholde resultatene for hver av dem. Nyttig når man skal tillate 
fasettering på flere ting samtidig.

Dersom bruker velger en fasett må et filter-kriterium for dette valget
også legges til i hovedspørringen, for å begrense (eller øke)
treffmengden.

### Kommuner og fylker

Fasettering på fylker og kommuner for alle aktive annonser.

Felter i ES for annonser `county_facet` og `municipal_facet`.

Spørringen under `"query": ...` kan være hva som helst i tillegg til 
standardfilteret som begrenser til aktive annonser. 

Endepunkt: `https://elasticsearch.nais.preprod.local/ad/_search`

```json
{
  "query": { "bool": { "filter": { "term": { "status": "ACTIVE" } } } },
  "aggs": {
    "counties": {
      "terms": { "field": "county_facet", "size": 20 },
      "aggs": {
        "municipals": { "terms": { "field": "municipal_facet", "size": 100 } },
        "municipals_missing": { "missing": { "field": "municipal_facet" } }
      }
    },
    "counties_missing": { "missing": { "field": "county_facet" } }
  }
}
```

Og med begresning til fylket Akershus i query:

```json
{
  "query": { "bool": { "filter": [
                          { "term": { "status": "ACTIVE" } },
                          { "term": { "county_facet": "AKERSHUS" } }
                     ] } },
  "aggs": {
    "counties": {
      "terms": { "field": "county_facet", "size": 20 },
      "aggs": {
        "municipals": { "terms": { "field": "municipal_facet", "size": 100 } },
        "municipals_missing": { "missing": { "field": "municipal_facet" } }
      }
    },
    "counties_missing": { "missing": { "field": "county_facet" } }
  }
}

```

Dette er kun et eksempel som viser et filter-kriterium for å aktiver en konkret 
fasettverdi, og søket forøvrig matcher alle aktive annonser. Svaret fra ES vil 
kun innholde en bucket for Akershus og tilhørende kommuner under 
`"aggregations": {...}`.

For å OR-e sammen flere fasettverdier må et bool-filter benyttes. Her
er et eksempel på filtrering på fylkene AKERSHUS eller OSLO:

```json
{
  "query": { "bool": { "filter": [
                          { "term": { "status": "ACTIVE" } },
			  { "bool": { "should": [ { "term": { "county_facet": "AKERSHUS" } },
						  { "term": { "county_facet": "OSLO" } } ]
	           		    	      } },
                     ] } },
  "aggs": {
    "counties": {
      "terms": { "field": "county_facet", "size": 20 },
      "aggs": {
        "municipals": { "terms": { "field": "municipal_facet", "size": 100 } },
        "municipals_missing": { "missing": { "field": "municipal_facet" } }
      }
    },
    "counties_missing": { "missing": { "field": "county_facet" } }
  }
}

```

Altså en ytre bool-spørring med filter-kriterier, og en indre
bool-spørring som velger enten "AKERSHUS" eller "OSLO". Kriterier i
"filter"-listen for "bool"-spørringer blir alltid kombinert med AND –
alle elementer i listen må matche for at dokumentet skal matche.


### Heltid/deltid

Fasettering på heltid/deltid for alle aktive annonser.

Felt i ES: `extent_facet`

```json
{
  "query": { "bool": { "filter": { "term": { "status": "ACTIVE" } } } },
  "aggs": {
    "extent": { "terms": { "field": "extent_facet" } },
    "extent_missing": {
      "missing" : { "field" : "extent_facet" }
    }
  }
}

```

### Engasjementstype (fast, vikariat, ettc.)

Dette er også kalt "stillingstype" i adreg.

Fasettering på engasjementstype for alle aktive annonser.

Felt i ES: `engagementtype_facet`

```json
{
  "query": { "bool": { "filter": { "term": { "status": "ACTIVE" } } } },
  "aggs": {
    "engagementtype": { "terms": { "field": "engagementtype_facet" } },
    "engagementtype_missing": {
      "missing" : { "field" : "engagementtype_facet" }
    }
  }
}

```

### Filtrere på tid

Det er enklest å benytte ElasticSearch "date math" for å filtrere på
tid.

Eksemplet viser filtrering til alle annonser som er opprettet/nye det
siste døgnet.

Felt i ES: `created`:

```json
{
  "query": {
    "bool": {
      "filter": {
        "range": {
          "created": {
            "gte": "now-1d"
          }
        }
      }
    }
  }
}
```

For å lage aggregation-buckets basert på tid:

```json
{
  "query": {
    "bool": {
      "filter": {
        "range": {
          "created": {
            "gte": "now-1d"
          }
        }
      }
    }
  },
  "aggs": {
    "range": {
      "date_range": {
        "field": "created",
        "ranges": [
          {
              "key": "last-day",
              "from": "now-1d"
          },
          {
              "key": "older",
              "to": "now-1d"
          }
        ]
      }
    }
  }
}
```

Se: https://www.elastic.co/guide/en/elasticsearch/reference/current/search-aggregations-bucket-daterange-aggregation.html


Forslag til yrkeskategorier (typeahead)
---------------------------------------

Følgende spørring kan fyres av hver gang bruker skriver et tegn, for å gi 
forslag til yrkeskategorier blant aktive annonser, der `"<INPUT>"` er hva 
brukeren har skrevet i felt så langt:

Felt i ES: `category_suggest`, tilhørende søkefelt heter `category_no` ("_no" 
for norsk språk).

Endepunkt: `/ad/_search`

Generell dokumentasjon om suggestion-API i ES:

https://www.elastic.co/guide/en/elasticsearch/reference/current/search-suggesters-completion.html

```json
{
  "suggest": {
    "yrkesgategori": {
      "prefix": "<INPUT>",
      "completion": {
        "field": "category_suggest",
        "contexts": {
          "status": "ACTIVE"
        },
        "size": 100
      }
    }
  },
  "_source": false
}

```

Svaret på suggest-spørringer ligger ikke under `hits` som for vanlige søk, men 
under `suggest`, og mer spesifikt i dette tilfellet under 
`suggest.yrkeskategori`. "yrkeskategori" er en label klienten kan velge selv, 
som identifiserer settet av forslag i responsen fra ES. Eksemplet over bruker 
source-filtering for at ikke kildedokumenter skal sendes med i forslagene, dvs. 
`_source` er satt til `false`, noe man typisk vil for typeahead-forslag.

Man kan be om flere forskjellige suggestions i samme spørring mot ES, noe vi 
trolig kommer til å få bruk for, for å vise hva slags type forslag det er til 
bruker.

Eksempel på flere typer suggestions i samme request:

```json
{
  "suggest": {
    "yrkesgategori": {
      "prefix": "<INPUT>",
      "completion": {
        "field": "category_suggest",
        "skip_duplicates": true,
        "contexts": {
          "status": "ACTIVE"
        },
        "size": 200
      }
    },
    "searchtags": {
      "prefix": "<INPUT>",
      "completion": {
        "field": "searchtags_suggest",
        "skip_duplicates": true,
        "contexts": {
          "status": "ACTIVE"
        },
        "size": 500
      }
    }
  },
  "_source": false
}

```


### Duplikater

Uten parameteren `skip_duplicates` vil man ofte får duplikate forslag
tilbake for denne typen spørringer. Parameteren er bare støttet i
ElasticSearch >= v6.1. Vi har ikke fått oppgradert til denne versjonen
i PAM testmiljø enda, så der må man gjøre deduplisering på klientsiden
inntil videre. Når alle miljøer er ferdig oppgradert kan man ta i bruk
denne parameteren. Gir store fordeler i forhold til størrelse på
response fra ES og at man slipper å gjøre deduplisering på klientside.
