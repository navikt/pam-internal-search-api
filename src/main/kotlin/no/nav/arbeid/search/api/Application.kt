package no.nav.arbeid.search.api

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("no.nav.arbeid.search.api")
                .mainClass(Application.javaClass)
                .start()
    }
}
