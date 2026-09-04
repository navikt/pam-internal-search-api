package no.nav.arbeid.search.api

import io.javalin.http.ContentType
import io.javalin.http.Context
import io.javalin.http.Header

internal fun Context.rawJson(json: String) {
    contentType(ContentType.APPLICATION_JSON).result(json)
}

internal fun Context.cachableJson(rawJson: String) {
    header(Header.CACHE_CONTROL, "public, max-age=300")
    rawJson(rawJson)
}
