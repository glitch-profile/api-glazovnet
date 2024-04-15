package net.glazov.di

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.server.config.*
import org.koin.dsl.module

private val databaseServerUrl = ApplicationConfig(null).tryGetString("glazov_net_server_data.base_url")

val ktorClientModule = module {
    single<HttpClient> {
        HttpClient(OkHttp) {
            expectSuccess = true
            defaultRequest {
                url(databaseServerUrl)
            }
        }
    }
}