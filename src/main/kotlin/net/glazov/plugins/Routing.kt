package net.glazov.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import net.glazov.routes.*

fun Application.configureRouting() {
    val apiKey = environment.config.property("storage.api_key").getString()

    routing {
        //testRoutes()
        postRoutes(apiKey)
        tariffsRoutes(apiKey)
        addressRoutes()
        clientsRoutes(apiKey)
        announcementsRoutes(apiKey)
    }
}
