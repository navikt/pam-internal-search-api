package no.nav.arbeid.search.api.rest

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Produces
@Singleton
class IllegalArgumentExceptionHandler : ExceptionHandler<IllegalArgumentException, HttpResponse<String>> {

    override fun handle(request: HttpRequest<Any?>, exception: IllegalArgumentException): HttpResponse<String> {
        return HttpResponse.badRequest(exception.message)
    }

}