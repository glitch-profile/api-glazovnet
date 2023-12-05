package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.AnnouncementsDataSource
import net.glazov.data.model.AnnouncementModel
import net.glazov.data.model.response.AnnouncementResponse
import net.glazov.data.model.response.SimpleResponse

private const val PATH = "/api/announcements"

fun Route.announcementsRoutes(
    serverApiKey: String,
    announcements: AnnouncementsDataSource
) {


    get("$PATH/getall") {
        val announcementsList = announcements.getAnnouncements()
        call.respond(
            AnnouncementResponse(
                status = true,
                message = "${announcementsList.size} announcements retrieved",
                data = announcementsList
            )
        )
    } //TODO: Remove after completion of announcements block

    get("$PATH/getforclient") {
        val clientLogin = call.request.queryParameters["login"] ?: ""
        val clientPassword = call.request.queryParameters["password"] ?: ""
        if (clientLogin.isNotBlank() && clientPassword.isNotBlank()) {
            val announcementsList = announcements.getAnnouncementForClient(
                clientLogin,
                clientPassword
            )
            call.respond(
                AnnouncementResponse(
                    status = true,
                    message = "announcements received",
                    data = announcementsList
                )
            )
        } else {
            call.respond(HttpStatusCode.BadRequest)
        }
    }

    post("$PATH/create") {
        val apiKey = call.request.queryParameters["api_key"]
        if (serverApiKey == apiKey) {
            val newAnnouncement = try {
                call.receive<AnnouncementModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val announcement = announcements.addAnnouncement(newAnnouncement)
            val status = announcement != null
            call.respond(
                AnnouncementResponse(
                    status = status,
                    message = if (status) "announcement added" else "error while adding the announcement",
                    data = if (status) listOf(announcement!!) else emptyList()
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    delete("$PATH/delete") {
        val apiKey = call.request.queryParameters["api_key"]
        if (serverApiKey == apiKey) {
            val announcementId = call.request.queryParameters["id"]
            if (announcementId != null) {
                val status = announcements.deleteAnnouncement(announcementId)
                call.respond(
                    AnnouncementResponse(
                        status = status,
                        message = if (status) "announcement deleted" else "error while deleting the announcement",
                        data = emptyList()
                    )
                )
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    put("$PATH/edit") {
        val apiKey = call.request.queryParameters["api_key"]
        if (serverApiKey == apiKey) {
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
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }
}