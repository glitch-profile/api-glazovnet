package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerializationException
import net.glazov.data.model.TariffModel
import net.glazov.data.response.SimpleTariffResponse
import net.glazov.database.addTariff
import net.glazov.database.deleteTariff
import net.glazov.database.getAllTariffs
import net.glazov.database.updateTariff

private const val APIKEY = "test_api_key_123"

fun Route.tariffsRoutes() {

    val path = "/api/tariffs"

    get("$path/getall") {
        val tariffStatus = call.request.queryParameters["tariffStatus"]
        val tariffs = getAllTariffs(status = tariffStatus.toBoolean())
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
        if (apiKey == APIKEY) {
            try {
                val newTariff = call.receive<TariffModel>()
                val status = addTariff(newTariff)
                call.respond(
                    SimpleTariffResponse(
                        status = status,
                        message = if (status) "tariff added" else "error while adding the tariff",
                        data = emptyList()
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
        if (apiKey == APIKEY) {
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
        if (apiKey == APIKEY) {
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