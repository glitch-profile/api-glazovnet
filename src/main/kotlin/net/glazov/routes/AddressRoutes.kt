package net.glazov.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.database.getCities
import net.glazov.database.getStreets

private const val PATH = "/api/addressinfo"

fun Route.addressRoutes() {

    get("$PATH/getcitieslist") {
        val name = call.request.queryParameters["name"]
        val citiesList = getCities(name)
        call.respond(citiesList.map { it.name })
    }

    get("$PATH/getstreetslist") {
        val filter = call.request.queryParameters["filter"]
        val streetsList = getStreets(filter)
        call.respond(streetsList.map { it.name })
    }

}