package net.glazov.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureAuthentication() {
    val issuer = environment.config.property("auth.issuer").getString()
    val secret = environment.config.property("auth.secret").getString()
    val authRealm = environment.config.property("auth.realm").getString()

    install(Authentication) {
        jwt {
            realm = authRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if ( (credential.payload.getClaim("person_id").asString()) != "" ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
    }
}