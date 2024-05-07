package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.AnnouncementsDataSource
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.model.AnnouncementModel
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.employeesroles.EmployeeRoles
import net.glazov.data.utils.notificationsmanager.*

private const val PATH = "/api/announcements"

fun Route.announcementsRoutes(
    announcements: AnnouncementsDataSource,
    notificationsManager: NotificationsManager,
    clients: ClientsDataSource,
    employees: EmployeesDataSource
) {

    authenticate("client") {

        get("$PATH/for-client") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val announcementsList = announcements.getAnnouncementForClient(clientId)
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "announcements received",
                    data = announcementsList
                )
            )
        }
    }

    authenticate("employee") {

        get(PATH) {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.ANNOUNCEMENTS)) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }
            val announcementsList = announcements.getAnnouncements()
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "${announcementsList.size} announcements retrieved",
                    data = announcementsList
                )
            )
        }

        post("$PATH/create") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.ANNOUNCEMENTS)) {
                call.respond(HttpStatusCode.Forbidden)
                return@post
            }
            val newAnnouncement = call.receiveNullable<AnnouncementModel>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val announcement = announcements.addAnnouncement(newAnnouncement)
            val status = announcement != null
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "announcement added" else "error while adding the announcement",
                    data = announcement
                )
            )
            if (announcement !== null) {
                if (announcement.addressFilters.isEmpty()) {
                    notificationsManager.sendTranslatableNotificationByTopic(
                        topic = NotificationsTopicsCodes.ANNOUNCEMENTS,
                        translatableData = TranslatableNotificationData.NewAnnouncements(
                            announcementTitle = announcement.title
                        ),
                        notificationChannel = NotificationChannel.Announcements,
                        deepLink = Deeplink.AnnouncementsList
                    )
                } else {
                    val affectedClients = announcements.getClientsForAnnouncement(announcement)
                    val affectedPersonsTokens = affectedClients.map { clients.getAssociatedPerson(it.id) }
                        .asSequence()
                        .filter {
                            it?.isNotificationsEnabled == true
                                    && it.selectedNotificationsTopics.contains(NotificationsTopicsCodes.ANNOUNCEMENTS.name)
                        }
                        .mapNotNull { it?.fcmTokensList }
                        .toList()
                    notificationsManager.sendTranslatableNotificationByTokens(
                        personsTokensList = affectedPersonsTokens,
                        translatableData = TranslatableNotificationData.NewAnnouncements(
                            announcementTitle = announcement.title
                        ),
                        notificationChannel = NotificationChannel.Announcements,
                        deepLink = Deeplink.AnnouncementsList
                    )
                }
            }
        }

        delete("$PATH/delete") {
            call.respond(HttpStatusCode.MethodNotAllowed)
            return@delete
//            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
//                call.respond(HttpStatusCode.BadRequest)
//                return@delete
//            }
//            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.ANNOUNCEMENTS)) {
//                call.respond(HttpStatusCode.Forbidden)
//                return@delete
//            }
//            val announcementId = call.request.queryParameters["id"]
//            if (announcementId != null) {
//                val status = announcements.deleteAnnouncement(announcementId)
//                call.respond(
//                    SimpleResponse(
//                        status = status,
//                        message = if (status) "announcement deleted" else "error while deleting the announcement",
//                        data = Unit
//                    )
//                )
//            } else {
//                call.respond(HttpStatusCode.BadRequest)
//            }
        }

        put("$PATH/edit") {
            call.respond(HttpStatusCode.MethodNotAllowed)
            return@put
//            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
//                call.respond(HttpStatusCode.BadRequest)
//                return@put
//            }
//            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.ANNOUNCEMENTS)) {
//                call.respond(HttpStatusCode.Forbidden)
//                return@put
//            }
//            val newAnnouncement = try {
//                call.receive<AnnouncementModel>()
//            } catch (e: ContentTransformationException) {
//                call.respond(HttpStatusCode.BadRequest)
//                return@put
//            }
//            val status = announcements.updateAnnouncement(newAnnouncement)
//            call.respond(
//                SimpleResponse(
//                    status = true,
//                    message = if (status) "announcement updated" else "error while updating announcement",
//                    data = status
//                )
//            )
        }
    }
}