package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.ClientsDataSource
import net.glazov.data.model.ClientModel
import net.glazov.data.model.response.SimpleResponse

private const val PATH = "/api/clients"

fun Route.clientsRoutes(
    clients: ClientsDataSource
) {
    authenticate {
        put("$PATH/update-fcm-token") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val token = call.response.headers["fcm_token"]
            val status = clients.updateFcmToken(
                userId = clientId,
                newToken = token
            )
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "token updated" else "failed to update token",
                    data = Unit
                )
            )
        }
    }

    authenticate("admin") {

        post("$PATH/create") {
            val clientModel = try {
                call.receive<ClientModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val newClient = clients.createClient(clientModel)
            val status = newClient != null
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "client added" else "error while adding the client",
                    data = newClient
                )
            )
        }

        get("$PATH/") {
            val clientsList = clients.getAllClients()
            call.respond(
                SimpleResponse (
                    status = true,
                    message = "clients retrieved",
                    data = clientsList
                )
            )
        }
    }
}