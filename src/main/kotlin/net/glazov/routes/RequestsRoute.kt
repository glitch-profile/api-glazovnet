package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import net.glazov.data.datasource.ChatDataSource
import net.glazov.data.datasourceimpl.RequestNotFoundException
import net.glazov.data.model.MessageModel
import net.glazov.data.model.SupportRequestModel
import net.glazov.data.model.response.SimpleResponse
import net.glazov.rooms.MemberAlreadyExistException
import net.glazov.rooms.RequestChatRoomController
import net.glazov.rooms.RequestsRoomController

private const val PATH = "/api/support"

fun Route.requestsRoute(
    serverApiKey: String,
    requestsRoomController: RequestsRoomController,
    requestChatRoomController: RequestChatRoomController,
    chat: ChatDataSource
) {

    authenticate {
        webSocket("$PATH/requests-socket") {
            val principal = call.principal<JWTPrincipal>()
            val memberId = principal!!.payload.getClaim("user_id").asString()
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
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized)
            }
        }

        webSocket("$PATH/requests/{request_id}/chat-socket") {
            val requestId = call.parameters["request_id"]
            val principal = call.principal<JWTPrincipal>()
            val memberId = principal!!.payload.getClaim("user_id").asString()
            val isAdmin = principal.payload.getClaim("is_admin").asBoolean()
            if (memberId != null && requestId != null) {
                try {
                    requestChatRoomController.onJoin(
                        requestId = requestId,
                        memberId = memberId,
                        isAdmin = isAdmin,
                        memberSocket = this
                    )
                    incoming.consumeEach {frame ->
                        if (frame is Frame.Text) {
                            try {
                                val messageText = frame.readText()
                                requestChatRoomController.sendMessage(
                                    requestId = requestId,
                                    senderId = memberId,
                                    message = messageText
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: MemberAlreadyExistException) {
                    call.respond(HttpStatusCode.Conflict)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    requestChatRoomController.tryDisconnect(
                        requestId = requestId,
                        memberId = memberId
                    )
                }
            } else call.respond(HttpStatusCode.BadRequest)
        }

        get("$PATH/requests/{request_id}") {
            val requestId = call.parameters["request_id"] ?: ""
            val principal = call.principal<JWTPrincipal>()
            val clientId = principal!!.payload.getClaim("user_id").asString()
            val isAdmin = principal.payload.getClaim("is_admin").asBoolean()
            val request = chat.getRequestById(requestId)
            if (request != null) {
                if (isAdmin || clientId == request.creatorId) {
                    val requestToRespond = request.copy(messages = emptyList())
                    call.respond(
                        SimpleResponse(
                            status = true,
                            message = "request retrieved",
                            data = requestToRespond
                        )
                    )
                } else call.respond(HttpStatusCode.Forbidden)
            } else call.respond(HttpStatusCode.NotFound)
        }

        get("$PATH/requests/{request_id}/messages") {
            val requestId = call.parameters["request_id"] ?: ""
            val principal = call.principal<JWTPrincipal>()
            val clientId = principal!!.payload.getClaim("user_id").asString()
            val isAdmin = principal.payload.getClaim("is_admin").asBoolean()
            val request = chat.getRequestById(requestId)
            if (request != null) {
                if (isAdmin || clientId == request.creatorId) {
                    val messages = request.messages.sortedByDescending { it.timestamp }
                    call.respond(
                        SimpleResponse(
                            status = true,
                            message = "request retrieved",
                            data = messages
                        )
                    )
                } else call.respond(HttpStatusCode.Forbidden)
            } else call.respond(HttpStatusCode.NotFound)
        }

        post("$PATH/create-request") {
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

        authenticate("admin") {

            get("$PATH/requests") {
                val requestsList = chat.getAllRequests(null)
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = "${requestsList.size} requests",
                        data = requestsList
                    )
                )
            }
        }
    }

}