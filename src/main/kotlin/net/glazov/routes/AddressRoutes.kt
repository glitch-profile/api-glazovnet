package net.glazov.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.database.getCitiesNames
import net.glazov.database.getStreetsForCity

private const val PATH = "/api/addressinfo"

fun Route.addressRoutes() {

    get("$PATH/getcitieslist") {
        val city = call.request.queryParameters["city"] ?: ""
        val citiesList = getCitiesNames(city)
        call.respond(citiesList)
    }

    get("$PATH/getstreetslist") {
        val city = call.request.queryParameters["city"]
        val street = call.request.queryParameters["street"]
        if (city !== null && street !== null) {
            val streetsList = getStreetsForCity(city, street)
            call.respond(streetsList.map { it.street })
        }
        call.respond(emptyList<String>())
    }

}