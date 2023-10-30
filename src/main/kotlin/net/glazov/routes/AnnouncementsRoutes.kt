package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.model.AnnouncementModel
import net.glazov.data.model.PostModel
import net.glazov.data.response.AnnouncementResponse
import net.glazov.database.addAnnouncement
import net.glazov.database.deleteAnnouncement
import net.glazov.database.getAnnouncementsByAddress

private const val PATH = "/api/announcements"

fun Route.announcementsRoutes(
    serverApiKey: String
) {

    get("$PATH/filterbyaddress") {
        val city = call.request.queryParameters["city"] ?: ""
        val street = call.request.queryParameters["street"] ?: ""
        val houseNumber = call.request.queryParameters["house_number"]?.toIntOrNull()
        if (city.isNotBlank() && street.isNotBlank() && houseNumber != null) {
            val announcements = getAnnouncementsByAddress(
                city = city,
                street = street,
                houseNumber = houseNumber
            )
            call.respond(announcements)
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