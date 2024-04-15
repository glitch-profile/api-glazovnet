package net.glazov.plugins

import io.ktor.server.application.*
import net.glazov.di.*
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {

    install(Koin) {
        modules(
            databaseModule,
            dataSourcesModule,
            roomControllersModule,
            utilsModule,
            ktorClientModule
        )
    }

}