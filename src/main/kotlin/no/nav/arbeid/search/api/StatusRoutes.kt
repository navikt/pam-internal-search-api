package no.nav.arbeid.search.api

import io.javalin.config.RoutesConfig
import io.javalin.http.ContentType
import io.javalin.http.Header
import io.javalin.http.HttpStatus
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.slf4j.LoggerFactory

private val LOG = LoggerFactory.getLogger("no.nav.arbeid.search.api.StatusRoutes")

private const val OPENSEARCH_TEST_QUERY = """{"query":{ "match_all": {} },"size": 1}"""

internal fun statusRoutes(
    routes: RoutesConfig,
    searchClient: SearchClient,
    registry: PrometheusMeterRegistry
) {
    listOf("/isAlive", "/isReady").forEach { path ->
        routes.get(path) { ctx -> ctx.header(Header.CACHE_CONTROL, "no-store").result("OK") }
    }

    routes.get("/amIOK") { ctx ->
        try {
            ctx.rawJson(searchClient.searchWithBody(INTERNALAD, emptyMap(), OPENSEARCH_TEST_QUERY))
        } catch (e: Exception) {
            LOG.error("Got exception", e)
            ctx.status(HttpStatus.FAILED_DEPENDENCY).result("NOTOK")
        }
    }

    routes.get("/prometheus") { ctx -> ctx.contentType(ContentType.TEXT_PLAIN).result(registry.scrape()) }
}
