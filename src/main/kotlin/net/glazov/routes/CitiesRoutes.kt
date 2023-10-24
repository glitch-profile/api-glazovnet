package net.glazov.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.database.getCities

private const val PATH = "/api/cities"

fun Route.citiesRoutes() {

    get("$PATH/getList") {
        val name = call.request.queryParameters["name"]
        val citiesList = getCities(name)
        call.respond(citiesList.map { it.name })
    }

}