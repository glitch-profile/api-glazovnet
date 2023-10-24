package net.glazov.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.database.getStreets

private const val PATH = "/api/streets"

fun Route.streetsRoutes() {

    get("$PATH/getList") {
        val name = call.request.queryParameters["name"]
        val streetsList = getStreets(name)
        call.respond(streetsList.map { it.name })
    }

}