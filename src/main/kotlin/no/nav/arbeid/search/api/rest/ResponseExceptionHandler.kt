package no.nav.arbeid.search.api.rest

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils
import jakarta.inject.Singleton
import org.opensearch.client.ResponseException

@Produces
@Singleton
class ResponseExceptionHandler : ExceptionHandler<ResponseException, HttpResponse<String>> {

    // Directly forward any elastic error responses with no wrapping (better for clients of pam-search-api)
    override fun handle(request: HttpRequest<Any?>, exception: ResponseException): HttpResponse<String> {
            val elasticResponse = exception.response
            val code = elasticResponse.statusLine.statusCode
            val entity = elasticResponse.entity
            return if (entity != null) {
                val ct = ContentType.getOrDefault(entity)
                val mt = MediaType(ct.toString())
                HttpResponse.status<String>(HttpStatus.valueOf(code))
                        .contentType(mt)
                        .body(EntityUtils.toString(entity))
            } else {
                HttpResponse.status<String>(HttpStatus.valueOf(code))
            }
    }

}