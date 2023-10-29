package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.model.ClientModel
import net.glazov.database.createClient

private const val PATH = "/api/clients"

fun Route.clientsRoutes(
    apiKeyServer: String
) {

    get("$PATH/getall") {

    }

    post("$PATH/create") {
        val apiKey = call.request.queryParameters["api_key"]
        if (apiKey == apiKeyServer) {
            val clientModel = try {
                call.receive<ClientModel>()

            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val newClient = createClient(clientModel)
            call.respond(newClient?.id ?: "Error while adding the client")
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

}