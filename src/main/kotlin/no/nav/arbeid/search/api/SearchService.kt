package no.nav.arbeid.search.api

import javax.inject.Singleton


@Singleton
class SearchService(private val searchClient: SearchClient) {

    fun searchWithBody(index: String, params: Map<String, MutableList<String>>, body: String): String {
        return searchClient.searchWithBody(index, params, body)
    }

    fun countWithBody(index: String, params: Map<String, MutableList<String>>, body: String): String {
        return searchClient.countWithBody(index, params, body)
    }

    fun searchWithQuery(index: String, params: Map<String, MutableList<String>>): String {
        return searchClient.searchWithQuery(index, params)
    }

    fun countWithQuery(index: String, params: Map<String, MutableList<String>>): String {
        return searchClient.countWithQuery(index, params)
    }


}
