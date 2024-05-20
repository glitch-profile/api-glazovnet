package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.model.users.ClientModel

private const val PATH = "/api/clients-management"

fun Route.clientsRoutes(
    clients: ClientsDataSource
) {

    authenticate("employee") {

        post("$PATH/create") {
            val client = call.receiveNullable<ClientModel>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val newClient = clients.addClient(
                associatedPersonId = client.personId,
                accountNumber = client.accountNumber,
                tariffId = client.tariffId,
                address = client.address
            )
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