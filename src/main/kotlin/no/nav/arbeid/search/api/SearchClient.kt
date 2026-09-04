package no.nav.arbeid.search.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.opensearch.client.opensearch.generic.OpenSearchGenericClient
import org.opensearch.client.opensearch.generic.Request
import org.opensearch.client.opensearch.generic.Requests
import org.opensearch.client.transport.OpenSearchTransport
import java.io.Closeable
import java.io.IOException

const val INTERNALAD = "internalad"
const val UNDERENHET = "underenhet"

/** Params a client is allowed to pass to document lookup. Anything else is rejected. */
private val ALLOWED_LOOKUP_PARAMS = setOf(
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
    "ccs_minimize_roundtrips"
)

class SearchClient(private val transport: OpenSearchTransport) : Closeable {

    private val genericClient =
        OpenSearchGenericClient(transport, null, OpenSearchGenericClient.ClientOptions.throwOnHttpErrors())

    private val mapper = ObjectMapper()

    fun searchWithBody(index: String, params: Map<String, List<String>>, body: String): String =
        execute("POST", "/$index/_search", params, body)

    fun countWithBody(index: String, params: Map<String, List<String>>, body: String): String =
        execute("POST", "/$index/_count", params, body)

    fun searchWithQuery(index: String, params: Map<String, List<String>>): String =
        execute("GET", "/$index/_search", params)

    fun countWithQuery(index: String, params: Map<String, List<String>>): String =
        execute("GET", "/$index/_count", params)

    fun lookup(documentId: String, onlySource: Boolean, params: Map<String, List<String>>): String {
        require(ALLOWED_LOOKUP_PARAMS.containsAll(params.keys)) { "Disallowed request params present in ${params.keys}" }

        val query = if (onlySource) params + ("filter_path" to listOf("_source")) else params
        val response = execute("GET", "/$INTERNALAD/_doc/$documentId", query)

        return if (onlySource) response.extractSource() else response
    }

    /** Unwraps `{"_source": {...}}` so a lookup with `/_source` returns the document itself. */
    private fun String.extractSource(): String = mapper.readTree(this).path("_source").toString()

    private fun execute(method: String, endpoint: String, params: Map<String, List<String>>, body: String? = null) =
        execute(
            Requests.builder()
                .endpoint(endpoint)
                .method(method)
                .query(params.mapValues { (_, values) -> values.joinToString(" ") })
                .apply { body?.let { json(it) } }
                .build()
        )

    private fun execute(request: Request): String =
        genericClient.execute(request).use { response ->
            response.body.orElseThrow { IOException("Empty response body from OpenSearch") }
                .use { it.bodyAsString() }
        }

    override fun close() = transport.close()
}
