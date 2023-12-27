package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.AddressesDataSource
import net.glazov.data.model.RegisteredAddressesModel
import net.glazov.data.model.response.SimpleResponse

private const val PATH = "/api/address-info"

fun Route.addressRoutes(
    serverApiKey: String,
    addresses: AddressesDataSource
) {
    authenticate {

        authenticate("admin") {

            get("$PATH/cities-list") {
                val apiKey = call.request.headers["api_key"]
                if (apiKey == serverApiKey) {
                    val city = call.request.queryParameters["city"] ?: ""
                    val citiesList = addresses.getCitiesNames(city)
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

            get("$PATH/streets-list") {
                val apiKey = call.request.headers["api_key"]
                if (apiKey == serverApiKey) {
                    val city = call.request.queryParameters["city"]
                    val street = call.request.queryParameters["street"]
                    if (city !== null && street !== null) {
                        val streetsList = addresses.getStreetsForCity(city, street)
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

            get("$PATH/addresses") {
                val apiKey = call.request.headers["api_key"]
                if (apiKey == serverApiKey) {
                    val city = call.request.queryParameters["city"]
                    val street = call.request.queryParameters["street"]
                    if (city != null && street != null) {
                        val addressesList = addresses.getAddresses(city, street)
                        val formattedAddresses = addressesList.map {
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
    }
}