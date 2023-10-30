package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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