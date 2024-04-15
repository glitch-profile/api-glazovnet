package net.glazov.di

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private val glazovNetHost = ApplicationConfig(null).tryGetString("glazov_net_server_data.host")
private val authLogin = ApplicationConfig(null).tryGetString("glazov_net_server_data.auth_login") ?: ""
private val authPass = ApplicationConfig(null).tryGetString("glazov_net_server_data.auth_pass") ?: ""

val ktorClientModule = module {
    single<HttpClient> {
        HttpClient(OkHttp) {
            defaultRequest {
                url(host = glazovNetHost)
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    }
                )
            }
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials(username = authLogin, password = authPass)
                    }
                    sendWithoutRequest { true }
                }
            }
            expectSuccess = true
        }
    }
}