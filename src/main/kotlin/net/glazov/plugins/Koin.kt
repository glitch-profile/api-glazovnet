package net.glazov.plugins

import io.ktor.server.application.*
import net.glazov.di.dataSourcesModule
import net.glazov.di.databaseModule
import net.glazov.di.roomControllersModule
import net.glazov.di.utilsModule
import org.koin.ktor.plugin.Koin

fun Application.configureKoin() {

    install(Koin) {
        modules(
            databaseModule,
            dataSourcesModule,
            roomControllersModule,
            utilsModule
        )
    }

}