package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.ClientsDataSource
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.notificationsmanager.NotificationsTopics

private const val PATH = "/api/notifications"

fun Route.notificationsRoutes(
    clients: ClientsDataSource
) {
    authenticate {

        get("$PATH/get-topics") {
            call.respond(NotificationsTopics.entries.map { it.name })
        }

        get("$PATH/get-topics-for-client") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val client = clients.getClientById(clientId)
            val topics = client?.selectedNotificationsTopics ?: emptyList()
            call.respond(
                SimpleResponse(
                    data = topics,
                    message = "retrieved topics for client",
                    status = client != null
                )
            )
        }

        get("$PATH/get-client-notifications-status") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val client = clients.getClientById(clientId)
            val notificationsStatus = client?.isNotificationsEnabled
            call.respond(
                SimpleResponse(
                    data = notificationsStatus,
                    message = "notifications status retrieved",
                    status = client != null
                )
            )
        }

        put("$PATH/set-client-notification-status") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val newNotificationsStatus = call.request.queryParameters["status"]
                ?.toBooleanStrictOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val result = clients.updateNotificationsStatus(clientId, newNotificationsStatus)
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "status updated" else "failed to update status",
                    data = Unit
                )
            )
        }

        put("$PATH/update-client-subscribed-topics") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val topics = call.request.queryParameters["topics"]
            val topicsList = topics?.split(',') ?: emptyList()
            val result = clients.updateNotificationTopics(clientId, topicsList)
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "topics updated" else "failed to update topics",
                    data = Unit
                )
            )
        }

        put("$PATH/update-client-fcm-token") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val token = call.request.queryParameters["fcm_token"]
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
}