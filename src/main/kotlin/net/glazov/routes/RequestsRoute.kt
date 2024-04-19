package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import net.glazov.data.datasource.ChatDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.model.requests.RequestsStatus
import net.glazov.data.model.requests.SupportRequestModel
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.employeesroles.EmployeeRoles
import net.glazov.rooms.MemberAlreadyExistException
import net.glazov.rooms.RequestChatRoomController
import net.glazov.rooms.RequestsRoomController

private const val PATH = "/api/support"

fun Route.requestsRoute(
    requestsRoomController: RequestsRoomController,
    requestChatRoomController: RequestChatRoomController,
    chat: ChatDataSource,
    employees: EmployeesDataSource
) {

    authenticate("client", "employee") {

        webSocket("$PATH/requests-socket") {
            val personId = call.request.headers["person_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@webSocket
            }
            try {
                requestsRoomController.onJoin(
                    memberId = personId,
                    socket = this
                )
                incoming.consumeEach { }
            } catch (e: MemberAlreadyExistException) {
                call.respond(HttpStatusCode.Conflict)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.ServiceUnavailable)
            } finally {
                requestsRoomController.tryDisconnect(personId)
            }
        }

        webSocket("$PATH/requests/{request_id}/chat-socket") {
            val personId = call.request.headers["person_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@webSocket
            }
            val requestId = call.parameters["request_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@webSocket
            }
            try {
                requestChatRoomController.onJoin(
                    requestId = requestId,
                    personId = personId,
                    memberSocket = this
                )
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        try {
                            val messageText = frame.readText()
                            requestChatRoomController.sendMessage(
                                requestId = requestId,
                                senderId = personId,
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
                    memberId = personId
                )
            }
        }

        get("$PATH/requests/{request_id}") {
            val requestId = call.parameters["request_id"] ?: ""
            val clientId = call.request.headers["client_id"]
            var isEmployeeWithRole = false
            call.request.headers["employee_Id"]?.let {
                isEmployeeWithRole = employees.checkEmployeeRole(it, EmployeeRoles.SUPPORT_CHAT)
            }
            val request = chat.getRequestById(requestId)
            if (request != null) {
                if (isEmployeeWithRole || clientId == request.creatorId) {
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
            val clientId = call.request.headers["client_id"]
            var isEmployeeWithRole = false
            call.request.headers["employee_Id"]?.let {
                isEmployeeWithRole = employees.checkEmployeeRole(it, EmployeeRoles.SUPPORT_CHAT)
            }
            val request = chat.getRequestById(requestId)
            if (request != null) {
                if (isEmployeeWithRole || clientId == request.creatorId) {
                    val messages = request.messages.sortedByDescending { it.timestamp }
                    call.respond(
                        SimpleResponse(
                            status = true,
                            message = "${messages.size} messages retrieved",
                            data = messages
                        )
                    )
                } else call.respond(HttpStatusCode.Forbidden)
            } else call.respond(HttpStatusCode.NotFound)
        }
    }

    authenticate("client") {

        get("$PATH/requests") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val requests = chat.getRequestsForClient(clientId)
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "requests retrieved",
                    data = requests
                )
            )
        }

        post("$PATH/create-request") {
            val newRequest = call.receiveNullable<SupportRequestModel>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val request = chat.createNewRequest(
                clientId = newRequest.creatorId,
                title = newRequest.title,
                text = newRequest.description,
                isNotificationEnabled = newRequest.isNotificationsEnabled
            )
            if (request != null) {
                requestsRoomController.sendRequestToSocket(request)
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = "request added",
                        data = request
                    )
                )
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }

    authenticate("employee") {

        get("$PATH/all-requests") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.SUPPORT_CHAT)) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }
            val requestsList = chat.getAllRequests(listOf(RequestsStatus.Active, RequestsStatus.InProgress))
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "${requestsList.size} requests",
                    data = requestsList
                )
            )
        }

        put("$PATH/requests/{request_id}/set-helper") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.SUPPORT_CHAT)) {
                call.respond(HttpStatusCode.Forbidden)
                return@put
            }
            val requestId = call.parameters["request_id"]
            if (requestId != null) {
                val status = chat.changeRequestHelper(requestId, employeeId)
                if (status) {
                    val request = chat.getRequestById(requestId)
                    requestsRoomController.sendRequestToSocket(request!!)
                }
                call.respond(
                    SimpleResponse(
                        status = status,
                        message = if (status) "request updated" else "failed to update request",
                        data = Unit
                    )
                )
            } else call.respond(HttpStatusCode.NotFound)
        }

        put("$PATH/requests/{request_id}/set-status") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.SUPPORT_CHAT)) {
                call.respond(HttpStatusCode.Forbidden)
                return@put
            }
            val requestId = call.parameters["request_id"]
            val newStatusCode = call.request.headers["new_status"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            if (requestId != null) {
                val status = chat.changeRequestStatus(requestId, newStatusCode.toInt())
                if (status) {
                    val request = chat.getRequestById(requestId)
                    requestsRoomController.sendRequestToSocket(request!!)
                }
                call.respond(
                    SimpleResponse(
                        status = status,
                        message = if (status) "request updated" else "failed to update request",
                        data = Unit
                    )
                )
            }
        }
    }

}