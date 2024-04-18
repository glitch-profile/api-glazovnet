package net.glazov.routes

import io.ktor.client.plugins.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import net.glazov.data.datasource.InnerDataSource
import net.glazov.data.model.response.SimpleResponse
import net.glazov.sessions.OAuthSession

private const val PATH = "/api/inner"

fun Route.innerRoutes(
    innerData: InnerDataSource
) {

    authenticate("employee") {

        authenticate("glazov-net-oauth") {

            get("$PATH/login") {
                // automatically redirects to glazov net auth server
            }

            get("$PATH/login-callback") {
                val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()
                println(currentPrincipal)
                currentPrincipal?.let { principal ->
                    principal.state?.let { state ->
                        call.sessions.set(
                            OAuthSession(state = state, token = principal.accessToken)
                        )

                    }
                }
            }

        }

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
                println("ERROR - ${e.stackTrace}")
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