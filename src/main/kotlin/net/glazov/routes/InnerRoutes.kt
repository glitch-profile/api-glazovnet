package net.glazov.routes

import io.ktor.client.plugins.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.InnerDataSource
import net.glazov.data.model.response.SimpleResponse

private const val PATH = "/api/inner"

fun Route.innerRoutes(
    innerData: InnerDataSource
) {

    authenticate("admin") {

        get("$PATH/posts") {
            try {
                val innerPostsResponse = innerData.getAllInnerPosts()
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = "inner posts retrieved",
                        data = innerPostsResponse
                    )
                )
            } catch (e: ResponseException) {
                call.respond(
                    SimpleResponse(
                        status = false,
                        message = e.response.status.toString(),
                        data = null
                    )
                )
            }
        }

    }

}