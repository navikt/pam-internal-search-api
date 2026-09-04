package no.nav.arbeid.search.api

import io.javalin.Javalin
import io.javalin.config.RoutesConfig
import io.javalin.http.ContentType
import io.javalin.http.Context
import io.javalin.http.Handler
import io.javalin.http.Header
import io.javalin.micrometer.MicrometerPlugin
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.opensearch.client.opensearch.generic.OpenSearchClientException
import org.slf4j.LoggerFactory
import java.io.IOException

private val LOG = LoggerFactory.getLogger("no.nav.arbeid.search.api.Application")

private const val OPENSEARCH_TEST_QUERY = """{"query":{ "match_all": {} },"size": 1}"""

fun main() {
    val config = AppConfig()
    val searchClient = SearchClient(openSearchTransport(config))

    createApp(searchClient, prometheusRegistry()).start(config.serverPort)
}

fun prometheusRegistry(): PrometheusMeterRegistry =
    PrometheusMeterRegistry(PrometheusConfig.DEFAULT).apply {
        ClassLoaderMetrics().bindTo(this)
        JvmMemoryMetrics().bindTo(this)
        JvmGcMetrics().bindTo(this)
        JvmThreadMetrics().bindTo(this)
        ProcessorMetrics().bindTo(this)
    }

fun createApp(searchClient: SearchClient, registry: PrometheusMeterRegistry): Javalin =
    Javalin.create { config ->
        config.startup.showJavalinBanner = false
        config.registerPlugin(MicrometerPlugin { it.registry = registry })
        config.bundledPlugins.enableCors { cors ->
            cors.addRule { rule ->
                rule.reflectClientOrigin = true
                rule.allowCredentials = true
                rule.maxAge = 4000
            }
        }

        config.routes.exception(OpenSearchClientException::class.java) { e, ctx ->
            // Forward opensearch error responses unwrapped (better for clients of this API)
            ctx.status(e.status())
            e.response().body.ifPresent { body ->
                body.use {
                    ctx.contentType(it.contentType() ?: ContentType.APPLICATION_JSON.mimeType)
                        .result(it.bodyAsString())
                }
            }
        }
        config.routes.exception(IllegalArgumentException::class.java) { e, ctx ->
            ctx.status(400).result(e.message ?: "Bad request")
        }
        config.routes.exception(IOException::class.java) { e, ctx ->
            LOG.error("Request to OpenSearch failed", e)
            ctx.status(502).result(e.message ?: "Bad gateway")
        }

        adRoutes(config.routes, searchClient)
        statusRoutes(config.routes, searchClient, registry)
    }

private fun adRoutes(routes: RoutesConfig, searchClient: SearchClient) {
    listOf("/internalad/_search", "/eures/internalad/_search").forEach { path ->
        routes.post(path) { ctx ->
            ctx.rawJson(searchClient.searchWithBody(INTERNALAD, ctx.queryParamMap(), ctx.body()))
        }
        routes.get(path) { ctx -> ctx.cachableJson(searchClient.searchWithQuery(INTERNALAD, ctx.queryParamMap())) }
    }

    listOf("/internalad/_count", "/eures/internalad/_count").forEach { path ->
        routes.post(path) { ctx ->
            ctx.rawJson(searchClient.countWithBody(INTERNALAD, ctx.queryParamMap(), ctx.body()))
        }
        routes.get(path) { ctx -> ctx.cachableJson(searchClient.countWithQuery(INTERNALAD, ctx.queryParamMap())) }
    }

    routes.post("/underenhet/_search") { ctx ->
        ctx.rawJson(searchClient.searchWithBody(UNDERENHET, ctx.queryParamMap(), ctx.body()))
    }
    routes.get("/underenhet/_search") { ctx ->
        ctx.cachableJson(searchClient.searchWithQuery(UNDERENHET, ctx.queryParamMap()))
    }

    listOf("/internalad/ad/{uuid}", "/eures/internalad/ad/{uuid}", "/stillingsok/ad/{uuid}").forEach { path ->
        routes.get(path, lookupHandler(searchClient, onlySource = false))
        routes.get("$path/_source", lookupHandler(searchClient, onlySource = true))
    }
}

private fun lookupHandler(searchClient: SearchClient, onlySource: Boolean) = Handler { ctx ->
    val uuid = ctx.pathParam("uuid")
    require(uuid.isNotBlank()) { "Missing or blank id: $uuid" }
    require(!uuid.contains("/")) { "Bad id: $uuid" }

    ctx.cachableJson(searchClient.lookup(uuid, onlySource, ctx.queryParamMap()))
}

private fun statusRoutes(
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
            ctx.status(424).result("NOTOK")
        }
    }

    routes.get("/prometheus") { ctx -> ctx.contentType(ContentType.TEXT_PLAIN).result(registry.scrape()) }
}

private fun Context.rawJson(json: String) {
    contentType(ContentType.APPLICATION_JSON).result(json)
}

private fun Context.cachableJson(rawJson: String) {
    header(Header.CACHE_CONTROL, "public, max-age=300")
    rawJson(rawJson)
}
