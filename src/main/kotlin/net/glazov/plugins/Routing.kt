package net.glazov.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.routes.postRoutes
import net.glazov.routes.tariffsRoutes
import net.glazov.routes.testRoutes

fun Application.configureRouting() {
    val apiKey = environment.config.property("storage.api_key").getString()

    routing {
        testRoutes()
        postRoutes(apiKey)
        tariffsRoutes(apiKey)
    }
}
