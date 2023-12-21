package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.TariffsDataSource
import net.glazov.data.model.TariffModel
import net.glazov.data.model.response.SimpleTariffResponse

fun Route.tariffsRoutes(
    apiKeyServer: String,
    tariffs: TariffsDataSource
) {

    val path = "/api/tariffs"

    get("$path/") {
        val tariffsList = tariffs.getAllTariffs()
        call.respond(
            SimpleTariffResponse(
                status = true,
                message = "${tariffsList.size} tariffs retrieved",
                data = tariffsList
            )
        )
    }

    post("$path/add") {
        val apiKey = call.request.headers["api_key"]
        if (apiKey == apiKeyServer) {
            try {
                val newTariff = call.receive<TariffModel>()
                val tariff = tariffs.addTariff(newTariff)
                val status = tariff != null
                call.respond(
                    SimpleTariffResponse(
                        status = status,
                        message = if (status) "tariff added" else "error while adding the tariff",
                        data = if (status) listOf(tariff!!) else emptyList()
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
        val apiKey = call.request.headers["api_key"]
        if (apiKey == apiKeyServer) {
            val tariffId = call.request.queryParameters["tariff_id"]
            val status = tariffs.deleteTariff(tariffId = tariffId.toString())
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
        val apiKey = call.request.headers["api_key"]
        if (apiKey == apiKeyServer) {
            val newTariff = try {
                call.receive<TariffModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val status = tariffs.updateTariff(newTariff)
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