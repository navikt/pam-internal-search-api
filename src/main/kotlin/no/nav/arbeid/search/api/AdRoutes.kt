package no.nav.arbeid.search.api

import io.javalin.config.RoutesConfig
import io.javalin.http.Handler

internal fun adRoutes(routes: RoutesConfig, searchClient: SearchClient) {
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
