package no.nav.arbeid.search.api

import javax.inject.Singleton

private val ALLOWED_REQUEST_PARAMS = setOf(
        "q",
        "filter_path",
        "pretty",
        "typed_keys",
        "ignore_unavailable",
        "expand_wildcards",
        "allow_no_indices",
        "ignore_throttled",
        "search_type",
        "batched_reduce_size",
        "ccs_minimize_roundtrips")

@Singleton
class SearchService(private val searchClient: SearchClient) {

    fun searchWithBody(index: String, params: Map<String, MutableList<String>>, body: String): String {
        require(onlyAllowedParams(params)) { "Disallowed request params present in " + params.keys }
        return searchClient.searchWithBody(index, params, body)
    }

    fun searchWithQuery(index: String, params: Map<String, MutableList<String>>): String {
        require(onlyAllowedParams(params)) { "Disallowed request params present in " + params.keys }
        return searchClient.searchWithQuery(index, params)
    }

    private fun onlyAllowedParams(params: Map<String, *>) = ALLOWED_REQUEST_PARAMS.containsAll(params.keys)

}
