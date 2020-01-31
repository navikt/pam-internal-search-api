package no.nav.arbeid.search.api.health

import io.micronaut.http.*
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import no.nav.arbeid.search.api.SearchService
import org.slf4j.LoggerFactory
import javax.inject.Inject


@Controller
class StatusController @Inject internal constructor(private val searchService: SearchService) {

    @Get("/isAlive")
    fun isAlive() = `basic response`()

    @Get("/isReady")
    fun isReady() = `basic response`()

    @Get("/amIOK")
    fun amIOK(): HttpResponse<String> {
        return try {
            return searchService.searchWithBody(emptyMap(), elasticTestQuery).let { HttpResponse.ok(it) }
        } catch (e: Exception) {
            LOG.error("Got exeption", e)
            HttpResponse.status(HttpStatus.FAILED_DEPENDENCY, "NOTOK")
        }
    }

    private fun `basic response`() = HttpResponse.ok("OK").headers {
        it.add(HttpHeaders.CACHE_CONTROL, HttpHeaderValues.CACHE_NO_STORE)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(StatusController::class.java)
    }

}

private const val elasticTestQuery =
        """
        {
            "query":{ "match_all": {} },
            "size": 1
        }
        """
