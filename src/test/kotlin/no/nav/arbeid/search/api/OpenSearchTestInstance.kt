package no.nav.arbeid.search.api

import org.opensearch.client.opensearch.generic.OpenSearchGenericClient
import org.opensearch.client.opensearch.generic.Requests
import org.opensearch.testcontainers.OpenSearchContainer
import org.testcontainers.utility.DockerImageName

// Bruk samme versjon som i dev og prod.
private const val OPENSEARCH_IMAGE = "opensearchproject/opensearch:2.19.5"

object OpenSearchTestInstance {

    private val container = OpenSearchContainer<Nothing>(DockerImageName.parse(OPENSEARCH_IMAGE))
        .also { it.start() }

    val config = AppConfig(
        openSearchUri = container.httpHostAddress,
        openSearchUsername = container.username,
        openSearchPassword = container.password
    )

    private val client = OpenSearchGenericClient(
        openSearchTransport(config),
        null,
        OpenSearchGenericClient.ClientOptions.throwOnHttpErrors()
    )

    fun index(index: String, id: String, document: String) {
        client.execute(
            Requests.builder()
                .endpoint("/$index/_doc/$id")
                .method("PUT")
                .query(mapOf("refresh" to "true"))
                .json(document)
                .build()
        ).close()
    }
}
