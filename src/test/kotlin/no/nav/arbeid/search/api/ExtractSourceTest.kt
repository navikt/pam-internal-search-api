package no.nav.arbeid.search.api

import no.nav.arbeid.search.api.SearchClient.Companion.extractSource
import org.junit.Assert.assertEquals
import org.junit.Test


class ExtractSourceTest {
    @Test
    fun testSourceExtract() {
        assertEquals(expectedOutput, inputString.extractSource())
    }

    private val inputString = """
            {
              "_source": {
                "id": 859528,
                "uuid": "bf500a48-caef-4cd6-87d1-a9ef35568009",
                "createdBy": "sbl",
                "updatedBy": "sbl",
                "created": "2023-09-29T08:54:00.417666",
                "updated": "2023-12-31T01:31:15.76173",
                "contactList": [],
                "mediaList": [],
                "locationList": [
                  {
                    "address": null,
                    "postalCode": null,
                    "country": "NORGE",
                    "county": "TROMS OG FINNMARK",
                    "municipal": "STORFJORD",
                    "city": null,
                    "latitude": null,
                    "longitude": null
                  }
                ],
                "properties": {
                  "extent": "Heltid",
                  "workhours": ""${'"'}["Natt"]""${'"'},
                  "workday": ""${'"'}["Søndag"]""${'"'},
                  "applicationdue": "Snarest",
                  "jobtitle": "Bandasjist",
                  "positioncount": "1",
                  "engagementtype": "Engasjement",
                  "classification_styrk08_score": "1.0",
                  "_approvedby": "AUTO",
                  "employerdescription": ""${'"'}<p>dad</p>
                  ""${'"'},
                  "_score": [
                    {
                      "name": "category",
                      "value": -50
                    },
                    {
                      "name": "location",
                      "value": -10
                    },
                    {
                      "name": "jobarrangement",
                      "value": -10
                    },
                    {
                      "name": "jobpercentage",
                      "value": -10
                    },
                    {
                      "name": "keywords",
                      "value": -10
                    },
                    {
                      "name": "applicationurl",
                      "value": -10
                    }
                  ],
                  "adtext": ""${'"'}<p>dad</p>
                  ""${'"'},
                  "classification_styrk08_code": "2221",
                  "hasInterestform": "false",
                  "searchtags": [
                    {
                      "label": "Bandasjist",
                      "score": 1
                    },
                    {
                      "label": "Spesialsykepleiere",
                      "score": 1
                    }
                  ],
                  "employer": "Barmhjertig Effektiv Tiger As",
                  "classification_esco_code": "http://data.europa.eu/esco/occupation/929b5c9d-2fbf-44f2-980d-2ac6c0dc212f",
                  "ontologyJobtitle": ""${'"'}{"konseptId":21656,"label":"Bandasjist","styrk08":"2221"}""${'"'},
                  "classification_input_source": "ontologyJobtitle",
                  "sector": "Ikke oppgitt",
                  "_scoretotal": "-100"
                },
                "title": "Dad",
                "status": "ACTIVE",
                "privacy": "SHOW_ALL",
                "source": "Stillingsregistrering",
                "medium": "Stillingsregistrering",
                "reference": "bf500a48-caef-4cd6-87d1-a9ef35568009",
                "published": "2023-09-29T00:00:00",
                "expires": "2023-12-31T00:00:00",
                "employer": {
                  "id": 769286,
                  "uuid": "31c42c49-f6bf-4152-8eda-baf57b14e7b7",
                  "createdBy": "pam-rekrutteringsbistand",
                  "updatedBy": "pam-ad",
                  "created": "2022-11-30T13:24:08.526224",
                  "updated": "2023-12-21T12:41:52.3621",
                  "contactList": [],
                  "mediaList": [],
                  "locationList": [
                    {
                      "address": "",
                      "postalCode": "",
                      "country": "",
                      "county": null,
                      "municipal": null,
                      "city": "",
                      "latitude": null,
                      "longitude": null
                    }
                  ],
                  "properties": {
                    "nace2": []
                  },
                  "name": "BARMHJERTIG EFFEKTIV TIGER AS",
                  "orgnr": "311493523",
                  "status": "ACTIVE",
                  "parentOrgnr": "310836907",
                  "publicName": "BARMHJERTIG EFFEKTIV TIGER AS",
                  "deactivated": null,
                  "orgform": "BEDR",
                  "employees": 8
                },
                "categoryList": [
                  {
                    "id": 2123219,
                    "code": "2221",
                    "categoryType": "STYRK08",
                    "name": "Spesialsykepleiere",
                    "description": "",
                    "parentId": null
                  },
                  {
                    "id": 2123220,
                    "code": "8111",
                    "categoryType": "STYRK08",
                    "name": "Bergfagarbeidere",
                    "description": "JANZZ-167815",
                    "parentId": null
                  },
                  {
                    "id": 2123222,
                    "code": "http://data.europa.eu/esco/occupation/4a215d87-8363-4db2-a661-3c0d816b2a70",
                    "categoryType": "ESCO",
                    "name": "\${'"'}gruvearbeider",
                    "description": "JANZZ-167815",
                    "parentId": null
                  },
                  {
                    "id": 2123218,
                    "code": "21656",
                    "categoryType": "JANZZ",
                    "name": "Bandasjist",
                    "description": "",
                    "parentId": null
                  },
                  {
                    "id": 2123221,
                    "code": "167815",
                    "categoryType": "JANZZ",
                    "name": "Dagbruddsoperatør",
                    "description": null,
                    "parentId": null
                  }
                ],
                "businessName": "Barmhjertig Effektiv Tiger As",
                "administration": {
                  "status": "DONE",
                  "comments": ""${'"'}Auto approved - 2023-09-29T08:54:00.374213212
                  ""${'"'},
                  "reportee": "F_Z994095 E_Z994095",
                  "remarks": [],
                  "navIdent": "Z994095"
                },
                "publishedByAdmin": "2023-09-29T08:54:00.374248",
                "occupationList": [
                  {
                    "level1": "Helse og sosial",
                    "level2": "Sykepleier"
                  },
                  {
                    "level1": "Industri og produksjon",
                    "level2": "Olje, gass og bergverk"
                  }
                ]
              }
            }
            """.trimIndent()

    private val expectedOutput = """
             {
                 "id": 859528,
                 "uuid": "bf500a48-caef-4cd6-87d1-a9ef35568009",
                 "createdBy": "sbl",
                 "updatedBy": "sbl",
                 "created": "2023-09-29T08:54:00.417666",
                 "updated": "2023-12-31T01:31:15.76173",
                 "contactList": [],
                 "mediaList": [],
                 "locationList": [
                   {
                     "address": null,
                     "postalCode": null,
                     "country": "NORGE",
                     "county": "TROMS OG FINNMARK",
                     "municipal": "STORFJORD",
                     "city": null,
                     "latitude": null,
                     "longitude": null
                   }
                 ],
                 "properties": {
                   "extent": "Heltid",
                   "workhours": ""${'"'}["Natt"]""${'"'},
                   "workday": ""${'"'}["Søndag"]""${'"'},
                   "applicationdue": "Snarest",
                   "jobtitle": "Bandasjist",
                   "positioncount": "1",
                   "engagementtype": "Engasjement",
                   "classification_styrk08_score": "1.0",
                   "_approvedby": "AUTO",
                   "employerdescription": ""${'"'}<p>dad</p>
                   ""${'"'},
                   "_score": [
                     {
                       "name": "category",
                       "value": -50
                     },
                     {
                       "name": "location",
                       "value": -10
                     },
                     {
                       "name": "jobarrangement",
                       "value": -10
                     },
                     {
                       "name": "jobpercentage",
                       "value": -10
                     },
                     {
                       "name": "keywords",
                       "value": -10
                     },
                     {
                       "name": "applicationurl",
                       "value": -10
                     }
                   ],
                   "adtext": ""${'"'}<p>dad</p>
                   ""${'"'},
                   "classification_styrk08_code": "2221",
                   "hasInterestform": "false",
                   "searchtags": [
                     {
                       "label": "Bandasjist",
                       "score": 1
                     },
                     {
                       "label": "Spesialsykepleiere",
                       "score": 1
                     }
                   ],
                   "employer": "Barmhjertig Effektiv Tiger As",
                   "classification_esco_code": "http://data.europa.eu/esco/occupation/929b5c9d-2fbf-44f2-980d-2ac6c0dc212f",
                   "ontologyJobtitle": ""${'"'}{"konseptId":21656,"label":"Bandasjist","styrk08":"2221"}""${'"'},
                   "classification_input_source": "ontologyJobtitle",
                   "sector": "Ikke oppgitt",
                   "_scoretotal": "-100"
                 },
                 "title": "Dad",
                 "status": "ACTIVE",
                 "privacy": "SHOW_ALL",
                 "source": "Stillingsregistrering",
                 "medium": "Stillingsregistrering",
                 "reference": "bf500a48-caef-4cd6-87d1-a9ef35568009",
                 "published": "2023-09-29T00:00:00",
                 "expires": "2023-12-31T00:00:00",
                 "employer": {
                   "id": 769286,
                   "uuid": "31c42c49-f6bf-4152-8eda-baf57b14e7b7",
                   "createdBy": "pam-rekrutteringsbistand",
                   "updatedBy": "pam-ad",
                   "created": "2022-11-30T13:24:08.526224",
                   "updated": "2023-12-21T12:41:52.3621",
                   "contactList": [],
                   "mediaList": [],
                   "locationList": [
                     {
                       "address": "",
                       "postalCode": "",
                       "country": "",
                       "county": null,
                       "municipal": null,
                       "city": "",
                       "latitude": null,
                       "longitude": null
                     }
                   ],
                   "properties": {
                     "nace2": []
                   },
                   "name": "BARMHJERTIG EFFEKTIV TIGER AS",
                   "orgnr": "311493523",
                   "status": "ACTIVE",
                   "parentOrgnr": "310836907",
                   "publicName": "BARMHJERTIG EFFEKTIV TIGER AS",
                   "deactivated": null,
                   "orgform": "BEDR",
                   "employees": 8
                 },
                 "categoryList": [
                   {
                     "id": 2123219,
                     "code": "2221",
                     "categoryType": "STYRK08",
                     "name": "Spesialsykepleiere",
                     "description": "",
                     "parentId": null
                   },
                   {
                     "id": 2123220,
                     "code": "8111",
                     "categoryType": "STYRK08",
                     "name": "Bergfagarbeidere",
                     "description": "JANZZ-167815",
                     "parentId": null
                   },
                   {
                     "id": 2123222,
                     "code": "http://data.europa.eu/esco/occupation/4a215d87-8363-4db2-a661-3c0d816b2a70",
                     "categoryType": "ESCO",
                     "name": "\${'"'}gruvearbeider",
                     "description": "JANZZ-167815",
                     "parentId": null
                   },
                   {
                     "id": 2123218,
                     "code": "21656",
                     "categoryType": "JANZZ",
                     "name": "Bandasjist",
                     "description": "",
                     "parentId": null
                   },
                   {
                     "id": 2123221,
                     "code": "167815",
                     "categoryType": "JANZZ",
                     "name": "Dagbruddsoperatør",
                     "description": null,
                     "parentId": null
                   }
                 ],
                 "businessName": "Barmhjertig Effektiv Tiger As",
                 "administration": {
                   "status": "DONE",
                   "comments": ""${'"'}Auto approved - 2023-09-29T08:54:00.374213212
                   ""${'"'},
                   "reportee": "F_Z994095 E_Z994095",
                   "remarks": [],
                   "navIdent": "Z994095"
                 },
                 "publishedByAdmin": "2023-09-29T08:54:00.374248",
                 "occupationList": [
                   {
                     "level1": "Helse og sosial",
                     "level2": "Sykepleier"
                   },
                   {
                     "level1": "Industri og produksjon",
                     "level2": "Olje, gass og bergverk"
                   }
                 ]
               }
            """.trimIndent()
}
