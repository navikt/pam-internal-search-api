package no.nav.arbeid.search.api

import io.javalin.Javalin
import io.javalin.micrometer.MicrometerPlugin
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

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

        exceptionHandlers(config.routes)
        adRoutes(config.routes, searchClient)
        statusRoutes(config.routes, searchClient, registry)
    }
