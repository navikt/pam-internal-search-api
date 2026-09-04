package no.nav.arbeid.search.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpServer
import io.javalin.testtools.HttpClient
import io.javalin.testtools.JavalinTest
import io.javalin.testtools.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress

class SearchApiIntegrationTest {

    @Test
    fun `POST search forwards body to opensearch`() {
        val response = post("/internalad/_search", MATCH_ALL)

        assertThat(response.code).isEqualTo(200)
        assertThat(response.json().totalHits()).isEqualTo(2)
    }

    @Test
    fun `GET search forwards query params to opensearch`() {
        val response = get("/internalad/_search?q=sykepleier")

        assertThat(response.code).isEqualTo(200)
        assertThat(response.json().uuids()).containsExactly("ad-1")
        assertThat(response.headers().get("Cache-Control")).containsExactly("public, max-age=300")
    }

    @Test
    fun `size param limits number of hits`() {
        val response = get("/internalad/_search?q=*&size=1")

        assertThat(response.json().totalHits()).isEqualTo(2)
        assertThat(response.json().uuids()).hasSize(1)
    }

    @Test
    fun `eures path searches the same index`() {
        assertThat(post("/eures/internalad/_search", MATCH_ALL).json().totalHits()).isEqualTo(2)
        assertThat(get("/eures/internalad/_search?q=bergen").json().uuids()).containsExactly("ad-2")
    }

    @Test
    fun `count returns number of matching documents`() {
        assertThat(post("/internalad/_count", MATCH_ALL).json()["count"].asInt()).isEqualTo(2)
        assertThat(get("/internalad/_count?q=bergen").json()["count"].asInt()).isEqualTo(1)
    }

    @Test
    fun `underenhet index is searchable`() {
        assertThat(post("/underenhet/_search", MATCH_ALL).json().totalHits()).isEqualTo(1)
        assertThat(get("/underenhet/_search?q=sykehjem").json().totalHits()).isEqualTo(1)
    }

    @Test
    fun `lookup returns the full opensearch document`() {
        val json = get("/internalad/ad/ad-1").json()

        assertThat(json["found"].asBoolean()).isTrue()
        assertThat(json["_source"]["uuid"].asText()).isEqualTo("ad-1")
    }

    @Test
    fun `lookup with _source unwraps the source document`() {
        val response = get("/internalad/ad/ad-1/_source")
        val json = response.json()

        assertThat(response.code).isEqualTo(200)
        assertThat(json.has("_source")).isFalse()
        assertThat(json["uuid"].asText()).isEqualTo("ad-1")
        assertThat(json["title"].asText()).isEqualTo("Sykepleier i Oslo")
    }

    @Test
    fun `lookup supports _source_includes`() {
        val json = get("/internalad/ad/ad-1/_source?_source_includes=uuid").json()

        assertThat(json.fieldNames().asSequence().toList()).containsExactly("uuid")
    }

    @Test
    fun `stillingsok path looks up in the internalad index`() {
        assertThat(get("/stillingsok/ad/ad-2").json()["_source"]["uuid"].asText()).isEqualTo("ad-2")
    }

    @Test
    fun `unknown document gives 404 from opensearch`() {
        val response = get("/internalad/ad/finnes-ikke")

        assertThat(response.code).isEqualTo(404)
        assertThat(response.json()["found"].asBoolean()).isFalse()
    }

    @Test
    fun `disallowed lookup params give 400`() {
        assertThat(get("/internalad/ad/ad-1?script_fields=evil").code).isEqualTo(400)
    }

    @Test
    fun `isAlive and isReady respond OK without caching`() {
        listOf("/isAlive", "/isReady").forEach { path ->
            val response = get(path)

            assertThat(response.code).isEqualTo(200)
            assertThat(response.body.string()).isEqualTo("OK")
            assertThat(response.headers().get("Cache-Control")).containsExactly("no-store")
        }
    }

    @Test
    fun `amIOK queries opensearch`() {
        val response = get("/amIOK")

        assertThat(response.code).isEqualTo(200)
        assertThat(response.json().totalHits()).isEqualTo(2)
    }

    @Test
    fun `opensearch rejection forwards the status instead of 502`() {
        // The transport wraps 401/403 in a plain IOException, hiding the status from the exception
        // type, so verify against a stub that rejects the way the OpenSearch proxy does.
        val opensearch = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0).apply {
            createContext("/") { exchange ->
                exchange.sendResponseHeaders(403, -1)
                exchange.close()
            }
            start()
        }
        val rejectedClient = SearchClient(
            openSearchTransport(AppConfig(openSearchUri = "http://127.0.0.1:${opensearch.address.port}"))
        )

        lateinit var response: Response
        try {
            JavalinTest.test(createApp(rejectedClient, prometheusRegistry())) { _, client ->
                response = client.post("/internalad/_search", MATCH_ALL)
            }
        } finally {
            rejectedClient.close()
            opensearch.stop(0)
        }

        assertThat(response.code).isEqualTo(403)
    }

    @Test
    fun `prometheus endpoint exposes metrics`() {
        assertThat(get("/prometheus").body.string()).contains("jvm_memory_used_bytes")
    }

    private fun get(path: String): Response = call { it.get(path) }

    private fun post(path: String, body: String): Response = call { it.post(path, body) }

    private fun call(request: (HttpClient) -> Response): Response {
        lateinit var response: Response
        JavalinTest.test(createApp(searchClient, prometheusRegistry())) { _, client -> response = request(client) }
        return response
    }

    private fun Response.json(): JsonNode = mapper.readTree(body.string())

    private fun JsonNode.totalHits() = this["hits"]["total"]["value"].asInt()

    private fun JsonNode.uuids() = this["hits"]["hits"].map { it["_source"]["uuid"].asText() }

    companion object {
        private const val MATCH_ALL = """{"query":{"match_all":{}}}"""

        private val mapper = ObjectMapper()

        private val searchClient by lazy { SearchClient(openSearchTransport(OpenSearchTestInstance.config)) }

        @JvmStatic
        @BeforeAll
        fun indexTestData() {
            OpenSearchTestInstance.index(
                INTERNALAD, "ad-1", """{"uuid":"ad-1","title":"Sykepleier i Oslo","status":"ACTIVE"}"""
            )
            OpenSearchTestInstance.index(
                INTERNALAD, "ad-2", """{"uuid":"ad-2","title":"Utvikler i Bergen","status":"ACTIVE"}"""
            )
            OpenSearchTestInstance.index(
                UNDERENHET, "912345678", """{"orgnr":"912345678","navn":"Oslo Sykehjem"}"""
            )
        }
    }
}
