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
    requestsRoomController: RequestsRoomController,
    chat: ChatDataSource
) {

    webSocket("$PATH/requests") {
        val session = call.sessions.get<ChatSession>()
        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "no session detected"))
            return@webSocket
        }
        try {
            requestsRoomController.onJoin(
                memberId = session.memberId,
                sessionId = session.sessionId,
                socket = this
            )
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {

                }
            }
        } catch (e: MemberAlreadyExistException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception) {
            call.respond(e.printStackTrace())
        } finally {
            requestsRoomController.tryDisconnect(session.memberId)
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
        }
    }

}