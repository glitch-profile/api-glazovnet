package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.model.RegisteredAddressesModel
import net.glazov.database.getAddresses
import net.glazov.database.getCitiesNames
import net.glazov.database.getStreetsForCity

private const val PATH = "/api/addressinfo"

fun Route.addressRoutes(
    serverApiKey: String
) {

    get("$PATH/getcitieslist") {
        val apiKey = call.request.queryParameters["api_key"]
        if (apiKey == serverApiKey) {
            val city = call.request.queryParameters["city"] ?: ""
            val citiesList = getCitiesNames(city)
            call.respond(citiesList)
        } else call.respond(HttpStatusCode.Forbidden)
    }

    get("$PATH/getstreetslist") {
        val apiKey = call.request.queryParameters["api_key"]
        if (apiKey == serverApiKey) {
            val city = call.request.queryParameters["city"]
            val street = call.request.queryParameters["street"]
            if (city !== null && street !== null) {
                val streetsList = getStreetsForCity(city, street)
                call.respond(streetsList.map { it.street })
            }
            call.respond(emptyList<String>())
        } else call.respond(HttpStatusCode.Forbidden)
    }

    get("$PATH/getaddresses") {
        val apiKey = call.request.queryParameters["api_key"]
        if (apiKey == serverApiKey) {
            val city = call.request.queryParameters["city"]
            val street = call.request.queryParameters["street"]
            if (city != null && street != null) {
                call.respond(getAddresses(city, street))
            } else call.respond(emptyList<RegisteredAddressesModel>())
        } else call.respond(HttpStatusCode.Forbidden)
    }

}