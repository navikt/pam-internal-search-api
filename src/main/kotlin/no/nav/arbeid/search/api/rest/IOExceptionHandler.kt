package no.nav.arbeid.search.api.rest

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Produces
import io.micronaut.http.server.exceptions.ExceptionHandler
import java.io.IOException
import javax.inject.Singleton

@Produces
@Singleton
class IOExceptionHandler: ExceptionHandler<IOException, HttpResponse<String>>{

    override fun handle(request: HttpRequest<*>, exception: IOException): HttpResponse<String>? {
        return HttpResponse.status(HttpStatus.BAD_GATEWAY, exception.message)
    }
}