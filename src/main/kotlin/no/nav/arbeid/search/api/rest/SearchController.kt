package no.nav.arbeid.search.api.rest

import io.micronaut.http.*
import io.micronaut.http.HttpHeaderValues.CACHE_MAX_AGE
import io.micronaut.http.HttpHeaderValues.CACHE_PUBLIC
import io.micronaut.http.HttpHeaders.CACHE_CONTROL
import io.micronaut.http.annotation.*
import no.nav.arbeid.search.api.*
import org.slf4j.LoggerFactory

@Controller
class SearchController constructor(private val searchService: SearchService, private val lookupService: LookupService) {

    companion object {
        private val LOG = LoggerFactory.getLogger(SearchController::class.java)
    }

    @Post(uris = ["/internalad/_search", "/eures/internalad/_search"])
    fun searchAdWithBody(params: HttpParameters, @Body body: String) = searchService.searchWithBody(INTERNALAD,params.asMap(), body)

    @Post(uris = ["/internalad/_count", "/eures/internalad/_count"])
    fun countAdWithBody(params: HttpParameters, @Body body: String) = searchService.countWithBody(INTERNALAD,params.asMap(), body)

    @Get(uris = ["/internalad/_search", "/eures/internalad/_search"])
    fun searchAdWithQuery(params: HttpParameters) = cachableResponse(searchService.searchWithQuery(INTERNALAD,params.asMap()))

    @Get(uris = ["/internalad/_count", "/eures/internalad/_count"])
    fun countAdWithQuery(params: HttpParameters) = cachableResponse(searchService.countWithQuery(INTERNALAD,params.asMap()))

    @Post(uris = ["/underenhet/_search"])
    fun searchUnderenhetWithBody(params: HttpParameters, @Body body: String) = searchService.searchWithBody(UNDERENHET,params.asMap(), body)

    @Get(uris = ["/underenhet/_search"])
    fun searchUnderenhetWithQuery(params: HttpParameters) = cachableResponse(searchService.searchWithQuery(UNDERENHET, params.asMap()))


    @Get(uris = ["internalad/ad/{uuid}", "internalad/ad/{uuid}/_source",
        "eures/internalad/ad/{uuid}", "eures/internalad/ad/{uuid}/_source",
        "/stillingsok/ad/{uuid}", "/stillingsok/ad/{uuid}/_source"])
    fun lookupAd(@PathVariable("uuid") uuid: String,
                 request: HttpRequest<*>): HttpResponse<String> {
        LOG.info("Validerer $uuid for søk etter ad")
        uuid.validateUuid()

        val onlySource = request.path.endsWith("/_source")

        return cachableResponse(lookupService.lookup(uuid, onlySource, request.parameters.asMap()))
    }



    private fun String.validateUuid() {

        require(isNotBlank()) { "Missing or blank id: $this" }
        require(!contains("/")) { "Bad id: $this" }
    }

    private fun cachableResponse(body: String) = HttpResponse.ok(body).headers { withDefaultCacheControl }

    // Cache-control header for GET queries
    private val withDefaultCacheControl = { it: MutableHttpHeaders ->
        it.add(CACHE_CONTROL, "$CACHE_PUBLIC, $CACHE_MAX_AGE=300")
    }


}

