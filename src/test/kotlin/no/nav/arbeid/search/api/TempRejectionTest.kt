package no.nav.arbeid.search.api

import com.sun.net.httpserver.HttpServer
import io.javalin.testtools.JavalinTest
import io.javalin.testtools.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress

class TempRejectionTest {

    @Test
    fun `opensearch rejection forwards the status instead of 502`() {
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
                response = client.post("/internalad/_search", """{"query":{"match_all":{}}}""")
            }
        } finally {
            rejectedClient.close()
            opensearch.stop(0)
        }

        assertThat(response.code).isEqualTo(403)
    }
}
