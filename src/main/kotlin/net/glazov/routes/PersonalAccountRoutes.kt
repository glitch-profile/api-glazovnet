package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.ClientsDataSource
import net.glazov.data.model.response.SimpleResponse

private const val PATH = "/api/account"

fun Route.personalAccountRoutes(
    clients: ClientsDataSource
) {

    authenticate {

        get("$PATH/info") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val clientInfo = clients.getClientById(clientId) ?: kotlin.run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "client info received",
                    data = clientInfo
                )
            )
        }

        put("$PATH/update-tariff") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val tariffId = call.request.headers["tariff_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val result = clients.changeTariff(userId = clientId, newTariffId = tariffId)
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "tariff updated" else "unable to update tariff",
                    data = Unit
                )
            )
        }

        put("$PATH/update-password") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val oldPassword = call.request.headers["old_password"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val newPassword = call.request.headers["new_password"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val result = clients.changeAccountPassword(
                userId = clientId,
                oldPassword = oldPassword,
                newPassword = newPassword
            )
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "password updated" else "unable to update password",
                    data = Unit
                )
            )
        }

        put("$PATH/block") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val result = clients.setIsAccountActive(
                userId = clientId,
                newStatus = false
            )
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "account blocked" else "unable to block account",
                    data = Unit
                )
            )

        }

        put("$PATH/add-funds") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val amount = call.request.headers["amount"]?.toDoubleOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val note = call.request.queryParameters["note"]
            try {
                clients.addPositiveTransaction(
                    userId = clientId,
                    amount = amount,
                    note = note
                )
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = "transaction complete",
                        data = Unit
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = e.message.toString(),
                        data = Unit
                    )
                )
            }
        }

    }

}