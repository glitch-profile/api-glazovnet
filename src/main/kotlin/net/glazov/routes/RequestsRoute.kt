package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import net.glazov.data.datasource.ChatDataSource
import net.glazov.data.datasource.PostsDataSource
import net.glazov.data.model.SupportRequestModel
import net.glazov.data.model.response.SimpleResponse
import net.glazov.rooms.MemberAlreadyExistException
import net.glazov.rooms.RequestsRoomController
import net.glazov.sessions.ChatSession

private const val PATH = "/api/support"

fun Route.requestsRoute(
    serverApiKey: String,
    requestsRoomController: RequestsRoomController,
    chat: ChatDataSource
) {

    webSocket("$PATH/requests-socket") {
//        val session = call.sessions.get<ChatSession>()
//        if (session == null) {
//            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "no session detected"))
//            return@webSocket
//        }
        val memberId = call.request.headers["memberId"]
        if (memberId != null) {
            try {
                requestsRoomController.onJoin(
                    memberId = memberId,
                    socket = this
                )
//                incoming.consumeEach { frame ->
//                    if (frame is Frame.Text) {
//
//                    }
//                }
            } catch (e: MemberAlreadyExistException) {
                call.respond(HttpStatusCode.Conflict)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.ServiceUnavailable)
            } finally {
                requestsRoomController.tryDisconnect(memberId)
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