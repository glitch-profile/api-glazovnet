package net.glazov.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json

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
                if ( (credential.payload.getClaim("user_id").asString()) != "" ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized)
            }
        }
        jwt("admin") {
            realm = authRealm
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("is_admin").asBoolean() == true) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Forbidden)
            }
        }
        oauth("glazov-net-oauth") {
            val httpClient = HttpClient(OkHttp) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        }
                    )
                }
            }
            urlProvider = { "http://localhost:8080/api/inner/login-callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "test",
                    authorizeUrl = "",
                    accessTokenUrl = "",
                    requestMethod = HttpMethod.Post,
                    clientId = "",
                    clientSecret = "",
//                    onStateCreated = { call, state ->
//                        call.request.queryParameters["redirectUrl"]?.let {
//
//                        }
//                    }
                )
            }
            client = httpClient
        }
    }
}