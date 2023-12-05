package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.ClientsDataSource
import net.glazov.data.model.ClientModel
import net.glazov.data.model.response.SimpleResponse

private const val PATH = "/api/clients"

fun Route.clientsRoutes(
    apiKeyServer: String,
    clients: ClientsDataSource
) {

    post("$PATH/create") {
        val apiKey = call.request.queryParameters["api_key"]
        if (apiKey == apiKeyServer) {
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
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    get("$PATH/getall") {
        val apiKey = call.request.queryParameters["api_key"]
        if (apiKey == apiKeyServer) {
            val clientsList = clients.getAllClients()
            call.respond(
                SimpleResponse (
                    status = true,
                    message = "clients retrieved",
                    data = clientsList
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }
}