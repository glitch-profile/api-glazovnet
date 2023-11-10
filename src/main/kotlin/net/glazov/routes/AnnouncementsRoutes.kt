package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.model.AnnouncementModel
import net.glazov.data.response.AnnouncementResponse
import net.glazov.database.*

private const val PATH = "/api/announcements"

fun Route.announcementsRoutes(
    serverApiKey: String
) {


    get("$PATH/getall") {
        val announcement = getAnnouncements()
        call.respond(
            AnnouncementResponse(
                status = true,
                message = "${announcement.size} announcements retrieved",
                data = announcement
            )
        )
    } //TODO: Remove after completion of announcements block

    get("$PATH/getforclient") {
        val clientLogin = call.request.queryParameters["login"]
        val clientPassword = call.request.queryParameters["password"]
        val clientId = login(clientLogin, clientPassword)
        if (clientId != null) {
            val announcements = getAnnouncementByClientId(clientId)
            call.respond(
                AnnouncementResponse(
                    status = true,
                    message = "announcements received",
                    data = announcements
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
            val announcement = addAnnouncement(newAnnouncement)
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
                val status = deleteAnnouncement(announcementId)
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
}