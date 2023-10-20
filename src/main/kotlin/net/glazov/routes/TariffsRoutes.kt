package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.model.TariffModel
import net.glazov.data.response.SimpleTariffResponse
import net.glazov.database.addTariff
import net.glazov.database.deleteTariff
import net.glazov.database.getAllTariffs
import net.glazov.database.updateTariff

fun Route.tariffsRoutes(
    apiKeyServer: String
) {

    val path = "/api/tariffs"

    get("$path/getall") {
        val tariffs = getAllTariffs()
        call.respond(
            SimpleTariffResponse(
                status = true,
                message = "${tariffs.size} tariffs retrieved",
                data = tariffs
            )
        )
    }

    post("$path/add") {
        val apiKey = call.request.queryParameters["api_key"]
        if (apiKey == apiKeyServer) {
            try {
                val newTariff = call.receive<TariffModel>()
                val tariff = addTariff(newTariff)
                call.respond(
                    SimpleTariffResponse(
                        status = tariff != null,
                        message = if (tariff != null) "tariff added" else "error while adding the tariff",
                        data = listOf(tariff)
                    )
                )
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    delete("$path/remove") {
        val apiKey = call.request.queryParameters["api_key"]
        if (apiKey == apiKeyServer) {
            val tariffId = call.request.queryParameters["tariff_id"]
            val status = deleteTariff(tariffId = tariffId.toString())
            call.respond(
                SimpleTariffResponse(
                    status = status,
                    message = if (status) "tariff deleted" else "no tariff found",
                    data = emptyList()
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    put("$path/edit") {
        val apiKey = call.request.queryParameters["api_key"]
        if (apiKey == apiKeyServer) {
            val newTariff = try {
                call.receive<TariffModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val status = updateTariff(newTariff)
            call.respond(
                SimpleTariffResponse(
                    status = status,
                    message = if (status) "tariff updated" else "error while updating the tariff",
                    data = emptyList()
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

}