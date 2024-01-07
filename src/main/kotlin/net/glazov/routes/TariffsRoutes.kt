package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.TariffsDataSource
import net.glazov.data.model.TariffModel
import net.glazov.data.model.response.SimpleTariffResponse

private const val PATH = "/api/tariffs"

fun Route.tariffsRoutes(
    tariffs: TariffsDataSource
) {

    authenticate {
        get("$PATH/") {
            val tariffsList = tariffs.getAllTariffs()
            call.respond(
                SimpleTariffResponse(
                    status = true,
                    message = "${tariffsList.size} tariffs retrieved",
                    data = tariffsList
                )
            )
        }

        authenticate("admin") {

            post("$PATH/add") {
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
            }

            delete("$PATH/remove") {
                val tariffId = call.request.queryParameters["tariff_id"]
                val status = tariffs.deleteTariff(tariffId = tariffId.toString())
                call.respond(
                    SimpleTariffResponse(
                        status = status,
                        message = if (status) "tariff deleted" else "no tariff found",
                        data = emptyList()
                    )
                )
            }

            put("$PATH/edit") {
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
            }
        }
    }
}