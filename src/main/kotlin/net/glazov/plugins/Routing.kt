package net.glazov.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.routes.testRoutes

fun Application.configureRouting() {
    routing {
        testRoutes() //Включаем нужный routes
    }
}
