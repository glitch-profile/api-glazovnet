package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.model.AnnouncementModel
import net.glazov.data.model.FilterModel
import net.glazov.data.response.FilterResponse
import net.glazov.database.addFilter
import net.glazov.database.deleteFilter
import net.glazov.database.getAllFilters

private const val PATH = "/api/filters"

fun Route.filtersRoutes(
    serverApiKey: String
) {

    get("$PATH/get") {
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()
        val filters = getAllFilters(limit)
        call.respond(
            FilterResponse(
                status = filters.isNotEmpty(),
                message = "${filters.size} filters found",
                data = filters
            )
        )
    }

    post("$PATH/create") {
        val apiKey = call.request.queryParameters["api_key"]
        if (serverApiKey == apiKey) {
            val newFilter = try {
                call.receive<FilterModel>()
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val filter = addFilter(newFilter)
            val status = filter != null
            call.respond(
                FilterResponse(
                    status = status,
                    message = if (status) "filter added" else "error while adding filter",
                    data = if (status) listOf(filter!!) else emptyList()
                )
            )
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

    delete("$PATH/delete") {
        val apiKey = call.request.queryParameters["api_key"]
        if (serverApiKey == apiKey) {
            val filterId = call.request.queryParameters["id"]
            if (filterId.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest)
            } else {
                val status = deleteFilter(filterId)
                call.respond(
                    FilterResponse(
                        status = status,
                        message = if (status) "filter deleted" else "error while deleting filter",
                        data = emptyList()
                    )
                )
            }
        } else {
            call.respond(HttpStatusCode.Forbidden)
        }
    }

}