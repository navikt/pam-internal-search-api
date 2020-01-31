package no.nav.arbeid.search.api

import io.micronaut.context.annotation.Value
import org.apache.http.entity.ContentType
import org.apache.http.entity.StringEntity
import org.apache.http.util.EntityUtils
import org.elasticsearch.client.Request
import org.elasticsearch.client.RestClientBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory
import java.net.URL
import javax.inject.Singleton

@Singleton
class SearchClient(
        client: RestClientBuilder,
        @Value("\${elasticsearch.url}") private val elasticsearchUrl: URL? = null
) : RestHighLevelClient(client) {

    private val index = "internalad"

    fun searchWithBody(params: Map<String, MutableList<String>>, body: String?): String {
        val request = Request("POST", "/$index/_search")
        params.forEach { (name, value) -> request.addParameter(name, value.joinToString(" ")) }
        request.entity = StringEntity(body, ContentType.APPLICATION_JSON)
        val responseEntity = lowLevelClient.performRequest(request).entity
        return EntityUtils.toString(responseEntity)
    }

    fun searchWithQuery(params: Map<String, MutableList<String>>): String {
        val request = Request("GET", "/$index/_search")
        params.forEach { (name, value) ->
            request.addParameter(name, value.joinToString(" ")) }
        val responseEntity = lowLevelClient.performRequest(request).entity
        return EntityUtils.toString(responseEntity)
    }

    fun lookup(documentId: String, onlySource: Boolean, params: Map<String, MutableList<String>>): String {
        val request = Request("GET", "/$index/_doc/$documentId" + if (onlySource) "/_source" else "")
        params.forEach { (name, value) -> request.addParameter(name, value.joinToString(" ")) }
        val responseEntity = lowLevelClient.performRequest(request).entity
        return EntityUtils.toString(responseEntity)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SearchClient::class.java)
    }

    init {
        LOG.info("Using Elasticsearch at {}", elasticsearchUrl)
    }
}