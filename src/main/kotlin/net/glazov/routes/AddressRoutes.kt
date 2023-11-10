package net.glazov.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.database.getCities
import net.glazov.database.getStreets

private const val PATH = "/api/addressinfo"

fun Route.addressRoutes() {

    get("$PATH/getcitieslist") {
        val city = call.request.queryParameters["name"]
        val citiesList = getCities(city)
        call.respond(citiesList.map { it.name })
    }

    get("$PATH/getstreetslist") {
        val city = call.request.queryParameters["city"]
        val street = call.request.queryParameters["name"]
        val streetsList = getStreets(city, street)
        call.respond(streetsList.map { it.name })
    }

}