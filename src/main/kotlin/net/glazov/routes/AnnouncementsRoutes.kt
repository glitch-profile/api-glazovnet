package net.glazov.routes

import com.google.firebase.messaging.AndroidNotification
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.AnnouncementsDataSource
import net.glazov.data.model.AnnouncementModel
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.notificationsmanager.NotificationsManager
import net.glazov.data.utils.notificationsmanager.NotificationsTopics
import net.glazov.data.utils.notificationsmanager.TranslatableNotificationData

private const val PATH = "/api/announcements"

fun Route.announcementsRoutes(
    announcements: AnnouncementsDataSource,
    notificationsManager: NotificationsManager
) {

    authenticate {

        get("$PATH/for-client") {
            val principal = call.principal<JWTPrincipal>()
            val clientId = principal!!.payload.getClaim("user_id").asString()
            val isAdmin = principal.payload.getClaim("is_admin").asBoolean()
            val announcementsList = announcements.getAnnouncementForClient(clientId, isAdmin)
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "announcements received",
                    data = announcementsList
                )
            )
        }
    }

    authenticate("admin") {

        get("$PATH/") {
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
                    notificationsManager.sendTranslatableNotificationToClientsByTopic(
                        topic = NotificationsTopics.ANNOUNCEMENTS,
                        translatableData = TranslatableNotificationData.NewAnnouncements(
                            announcementTitle = announcement.title
                        ),
                        priority = AndroidNotification.Priority.HIGH
                    )
                } else {
                    val affectedClientsTokens = announcements.getClientsForAnnouncement(announcement)
                        .asSequence()
                        .filter { it.isNotificationsEnabled == true
                                && it.selectedNotificationsTopics?.contains(NotificationsTopics.ANNOUNCEMENTS.name) == true
                        }
                        .mapNotNull { it.fcmTokensList }
                    notificationsManager.sendTranslatableNotificationToClientsByTokens(
                        clientsTokensLists = affectedClientsTokens.toList(),
                        translatableData = TranslatableNotificationData.NewAnnouncements(
                            announcementTitle = announcement.title
                        ),
                        priority = AndroidNotification.Priority.HIGH
                    )
                }
            }
        }

        delete("$PATH/delete") {
            val announcementId = call.request.queryParameters["id"]
            if (announcementId != null) {
                val status = announcements.deleteAnnouncement(announcementId)
                call.respond(
                    SimpleResponse(
                        status = status,
                        message = if (status) "announcement deleted" else "error while deleting the announcement",
                        data = Unit
                    )
                )
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        put("$PATH/edit") {
            val newAnnouncement = try {
                call.receive<AnnouncementModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val status = announcements.updateAnnouncement(newAnnouncement)
            call.respond(
                SimpleResponse(
                    status = true,
                    message = if (status) "announcement updated" else "error while updating announcement",
                    data = status
                )
            )
        }
    }
}