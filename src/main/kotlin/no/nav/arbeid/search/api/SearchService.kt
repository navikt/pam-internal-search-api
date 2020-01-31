package no.nav.arbeid.search.api

import javax.inject.Singleton

private val ALLOWED_REQUEST_PARAMS = setOf("q", "filter_path", "pretty")

@Singleton
class SearchService(private val searchClient: SearchClient) {

    fun searchWithBody(params: Map<String, MutableList<String>>, body: String): String {
        require(onlyAllowedParams(params)) { "Disallowed request params present in " + params.keys }
        return searchClient.searchWithBody(params, body)
    }

    fun searchWithQuery(params: Map<String, MutableList<String>>): String {
        require(onlyAllowedParams(params)) { "Disallowed request params present in " + params.keys }
        return searchClient.searchWithQuery(params)
    }

    private fun onlyAllowedParams(params: Map<String, *>) = ALLOWED_REQUEST_PARAMS.containsAll(params.keys)

}