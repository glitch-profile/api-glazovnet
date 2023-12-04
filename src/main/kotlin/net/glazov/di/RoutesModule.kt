package net.glazov.di

import io.ktor.server.config.*
import org.koin.dsl.module

private val serverApiKey = ApplicationConfig(null).tryGetString("storage.api_key").toString()

val routesModule = module {  }
//TODO