package net.glazov.routes

import io.ktor.server.routing.*

private const val PATH = "/api/clients"

fun Route.clientsRoutes(
    apiKeyServer: String
) {

    get("$PATH/getall") {

    }

}