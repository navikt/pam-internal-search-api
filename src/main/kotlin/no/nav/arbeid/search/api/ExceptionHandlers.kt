package no.nav.arbeid.search.api

import io.javalin.config.RoutesConfig
import io.javalin.http.ContentType
import io.javalin.http.HttpStatus
import org.opensearch.client.opensearch.generic.OpenSearchClientException
import org.opensearch.client.transport.httpclient5.ResponseException
import org.slf4j.LoggerFactory
import java.io.IOException

private val LOG = LoggerFactory.getLogger("no.nav.arbeid.search.api.ExceptionHandlers")

internal fun exceptionHandlers(routes: RoutesConfig) {
    routes.exception(OpenSearchClientException::class.java) { e, ctx ->
        // Forward opensearch error responses unwrapped (better for clients of this API)
        ctx.status(e.status())
        e.response().body.ifPresent { body ->
            body.use {
                ctx.contentType(it.contentType() ?: ContentType.APPLICATION_JSON.mimeType)
                    .result(it.bodyAsString())
            }
        }
    }

    routes.exception(IllegalArgumentException::class.java) { e, ctx ->
        ctx.status(HttpStatus.BAD_REQUEST).result(e.message ?: "Bad request")
    }

    routes.exception(IOException::class.java) { e, ctx ->
        // 401 and 403 responses from OpenSearch are wrapped in an IOException->TransportException->ResponseException
        val rejected = generateSequence(e as Throwable) { it.cause }
            .filterIsInstance<ResponseException>()
            .firstOrNull()
        if (rejected != null) {
            LOG.warn("OpenSearch rejected request with status {}", rejected.status())
            ctx.status(rejected.status()).result(HttpStatus.forStatus(rejected.status()).message)
        } else {
            LOG.error("Request to OpenSearch failed", e)
            ctx.status(HttpStatus.BAD_GATEWAY).result(e.message ?: "Bad gateway")
        }
    }
}
