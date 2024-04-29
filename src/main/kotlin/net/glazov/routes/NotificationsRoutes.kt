package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.notificationsmanager.NotificationTopic
import net.glazov.data.utils.notificationsmanager.NotificationsTopicsCodes

private const val PATH = "/api/notifications"

fun Route.notificationsRoutes(
    persons: PersonsDataSource,
    clients: ClientsDataSource,
    employees: EmployeesDataSource
) {
    authenticate {

        get("$PATH/topics") {
            val clientId = call.request.headers["client_id"]
            val employeeId = call.request.headers["employee_id"]
            val includeClientTopics = if (clientId != null) {
                clients.getClientById(clientId) ?: kotlin.run {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get
                }
                true
            } else false
            val includeEmployeeTopics = if (employeeId != null) {
                employees.getEmployeeById(employeeId) ?: kotlin.run {
                    call.respond(HttpStatusCode.Forbidden)
                    return@get

                }
                true
            } else false
            call.respond(
                SimpleResponse(
                    data = NotificationTopic.all(
                        includeClientTopics = includeClientTopics,
                        includeEmployeeTopics = includeEmployeeTopics
                    ),
                    status = true,
                    message = "topics retrieved"
                )
            )
        }

        get("$PATH/get-topics-for-person") {
            val personId = call.request.headers["person_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val person = persons.getPersonById(personId)
            val topics = person?.selectedNotificationsTopics ?: emptyList()
            call.respond(
                SimpleResponse(
                    data = topics,
                    message = "retrieved topics for person",
                    status = person != null
                )
            )
        }

        get("$PATH/get-person-notifications-status") {
            val personId = call.request.headers["person_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val person = persons.getPersonById(personId)
            val notificationsStatus = person?.isNotificationsEnabled
            call.respond(
                SimpleResponse(
                    data = notificationsStatus,
                    message = "notifications status retrieved",
                    status = person != null
                )
            )
        }

        put("$PATH/set-person-notifications-status") {
            val personId = call.request.headers["person_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val newNotificationsStatus = call.request.queryParameters["status"]
                ?.toBooleanStrictOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val result = persons.updateNotificationsStatus(personId, newNotificationsStatus)
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "status updated" else "failed to update status",
                    data = Unit
                )
            )
        }

        put("$PATH/update-person-subscribed-topics") {
            val personId = call.request.headers["person_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val topics = call.request.queryParameters["topics"]
            val clientId = call.request.headers["client_id"]
            val employeeId = call.request.headers["employee_id"]
            val includeClientTopics = if (clientId != null) {
                clients.getClientById(clientId) ?: kotlin.run {
                    call.respond(HttpStatusCode.Forbidden)
                    return@put
                }
                true
            } else false
            val includeEmployeeTopics = if (employeeId != null) {
                employees.getEmployeeById(employeeId) ?: kotlin.run {
                    call.respond(HttpStatusCode.Forbidden)
                    return@put

                }
                true
            } else false
            val topicsList = topics?.split(',') ?: emptyList()
            try {
                val topicsFormatted = topicsList.map { NotificationsTopicsCodes.valueOf(it) }
                val availableTopics = NotificationTopic.all(includeClientTopics, includeEmployeeTopics).map { it.topicCode }
                val topicsToSet = topicsFormatted.filter { availableTopics.contains(it) }
                val result = persons.updateNotificationTopics(personId, topicsToSet)
                call.respond(
                    SimpleResponse(
                        status = result,
                        message = if (result) "topics updated" else "failed to update topics",
                        data = Unit
                    )
                )
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
        }

        put("$PATH/update-person-fcm-token") {
            val personId = call.request.headers["person_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val token = call.request.headers["fcm_token"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val isExclude = call.request.queryParameters["exclude"]?.toBooleanStrictOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val status = if (isExclude) {
                persons.removeFcmToken(personId, tokenToRemove = token)
            } else {
                persons.addFcmToken(personId, newToken = token)
            }
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