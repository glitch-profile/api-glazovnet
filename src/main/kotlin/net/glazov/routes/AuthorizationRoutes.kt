package net.glazov.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.AdminsDataSource
import net.glazov.data.datasource.ClientsDataSource
import net.glazov.data.model.AdminModel
import net.glazov.data.model.auth.AuthModel
import net.glazov.data.model.auth.AuthResponseModel
import net.glazov.data.model.response.SimpleResponse
import java.time.OffsetDateTime
import java.time.ZoneId

private const val PATH = "/api"

fun Routing.authRoutes(
    clientsDataSource: ClientsDataSource,
    adminsDataSource: AdminsDataSource
) {

    val issuer = ApplicationConfig(null).tryGetString("auth.issuer").toString()
    val secret = ApplicationConfig(null).tryGetString("auth.secret").toString()

    post("$PATH/login") {
        val authData = try {
            call.receive<AuthModel>()
        } catch (e: ContentTransformationException) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        var generatedToken: String? = null
        var userId: String? = null
        var isAdmin: Boolean? = null

        if (authData.asAdmin) {
            val admin = adminsDataSource.login(authData.username, authData.password)
            if (admin != null) {
                userId = admin.id
                isAdmin = true
                val expireDateInstant = OffsetDateTime.now(ZoneId.systemDefault()).plusMonths(6).toInstant()
                val token = JWT.create()
                    .withIssuer(issuer)
                    .withClaim("user_id", admin.id)
                    .withClaim("is_admin", true)
                    .withExpiresAt(expireDateInstant)
                    .sign(Algorithm.HMAC256(secret))
                generatedToken = token
            }
        } else {
            val client = clientsDataSource.login(authData.username, authData.password)
            if (client != null) {
                userId = client.id
                isAdmin = false
                val token = JWT.create()
                    .withIssuer(issuer)
                    .withClaim("user_id", client.id)
                    .withClaim("is_admin", false)
                    .sign(Algorithm.HMAC256(secret))
                generatedToken = token
            }
        }
        if (generatedToken != null) {
            val authResponse = AuthResponseModel(
                token = generatedToken,
                userId = userId!!,
                isAdmin = isAdmin!!
            )
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "user logged in",
                    data = authResponse
                )
            )
        } else call.respond(
            SimpleResponse(
                status = false,
                message = "user not found",
                data = null
            )
        )
    }

    post("$PATH/add-admin") {
        val admin = try {
            call.receive<AdminModel>()
        } catch (e: ContentTransformationException) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val result = adminsDataSource.addAdmin(admin)
        if (result != null) call.respond(
            SimpleResponse(
                status = true,
                message = "admin created",
                data = result
            )
        )
        else call.respond(
            SimpleResponse(
                status = false,
                message = "admin was not created",
                data = null
            )
        )
    }

    authenticate {

        get("$PATH/check") {
            val principal = call.principal<JWTPrincipal>()
            val clientId = principal!!.payload.getClaim("user_id").asString()
            call.respond("Hello, $clientId. Are you not an admin...")
        }

    }

    authenticate("admin") {

        get("$PATH/check-adm") {
            val principal = call.principal<JWTPrincipal>()
            val isAdmin = principal!!.payload.getClaim("is_admin").asBoolean()
            if (isAdmin) {
                val clientId = principal.payload.getClaim("user_id").toString()
                call.respond("Hello, $clientId. Are you an admin!")
            } else call.respond(HttpStatusCode.Forbidden)
        }

    }
}