package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.model.RegisteredAddressesModel
import net.glazov.data.response.SimpleResponse
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
            val formattedCitiesList = citiesList.map { it.replaceFirstChar { it.uppercaseChar() } }
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "streets retrieved",
                    data = formattedCitiesList
                )
            )
        } else call.respond(HttpStatusCode.Forbidden)
    }

    get("$PATH/getstreetslist") {
        val apiKey = call.request.queryParameters["api_key"]
        if (apiKey == serverApiKey) {
            val city = call.request.queryParameters["city"]
            val street = call.request.queryParameters["street"]
            if (city !== null && street !== null) {
                val streetsList = getStreetsForCity(city, street)
                val formattedStreetsNames = streetsList.map {
                    it.street.replaceFirstChar { it.uppercaseChar() }
                }
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = "streets retrieved",
                        data = formattedStreetsNames
                    )
                )
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }

        } else call.respond(HttpStatusCode.Forbidden)
    }

    get("$PATH/getaddresses") {
        val apiKey = call.request.queryParameters["api_key"]
        if (apiKey == serverApiKey) {
            val city = call.request.queryParameters["city"]
            val street = call.request.queryParameters["street"]
            if (city != null && street != null) {
                val addresses = getAddresses(city, street)
                val formattedAddresses = addresses.map {
                    it.copy(
                        city = it.city.replaceFirstChar { it.uppercaseChar() },
                        street = it.street.replaceFirstChar { it.uppercaseChar() }
                    )
                }
                call.respond(
                    message = SimpleResponse(
                        status = true,
                        message = "addresses retrieved",
                        data = formattedAddresses
                    ),
                    status = HttpStatusCode.OK
                )
            } else call.respond(
                message = SimpleResponse(
                    status = true,
                    message = "addresses retrieved",
                    data = emptyList<List<RegisteredAddressesModel>>()
                )
            )
        } else call.respond(HttpStatusCode.Forbidden)
    }

}