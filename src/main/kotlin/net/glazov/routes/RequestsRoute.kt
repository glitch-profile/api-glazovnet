package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.channels.consumeEach
import net.glazov.data.datasource.ChatDataSource
import net.glazov.data.model.SupportRequestModel
import net.glazov.data.model.response.SimpleResponse
import net.glazov.rooms.MemberAlreadyExistException
import net.glazov.rooms.RequestsRoomController

private const val PATH = "/api/support"

fun Route.requestsRoute(
    serverApiKey: String,
    requestsRoomController: RequestsRoomController,
    chat: ChatDataSource
) {

    webSocket("$PATH/requests-socket") {
        val memberId = call.request.headers["memberId"]
        if (memberId != null) {
            try {
                requestsRoomController.onJoin(
                    memberId = memberId,
                    socket = this
                )
                incoming.consumeEach {  }
            } catch (e: MemberAlreadyExistException) {
                call.respond(HttpStatusCode.Conflict)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.ServiceUnavailable)
            } finally {
                requestsRoomController.tryDisconnect(memberId)
                println("connection closed")
            }
        } else {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }

    get("$PATH/requests") {
        val apiKey = call.request.headers["api_key"]
        if (apiKey == serverApiKey) {
            val requestsList = chat.getAllRequests(true)
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "${requestsList.size} requests",
                    data = requestsList
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    post("$PATH/createrequest") {
        val newRequest = try {
            call.receive<SupportRequestModel>()
        } catch (e: ContentTransformationException) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val request = chat.createNewRequest(newRequest)
        if (request != null) {
            requestsRoomController.addRequest(request)
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "request added",
                    data = request
                )
            )
        } else {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }

}