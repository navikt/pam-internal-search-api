package no.nav.arbeid.search.api

import javax.inject.Singleton

@Singleton
class LookupService(private val searchClient: SearchClient) {

    fun lookup(documentId: String, onlySource: Boolean, params: Map<String, MutableList<String>>): String {
        require(onlyAllowedParams(params)) { "Disallowed request params present in " + params.keys }
        return searchClient.lookup(documentId, onlySource, params)
    }

    private val allowedRequestParams = setOf(
            "_source",
            "_source_include",
            "_source_exclude",
            "_source_includes",
            "_source_excludes",
            "filter_path",
            "typed_keys",
            "ignore_unavailable",
            "expand_wildcards",
            "allow_no_indices",
            "ignore_throttled",
            "search_type",
            "batched_reduce_size",
            "ccs_minimize_roundtrips")

    private fun onlyAllowedParams(params: Map<String, *>) = allowedRequestParams.containsAll(params.keys)

}