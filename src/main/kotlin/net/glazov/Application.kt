package net.glazov

import io.ktor.server.application.*
import net.glazov.di.dataSourcesModule
import net.glazov.di.databaseModule
import net.glazov.di.routesModule
import net.glazov.plugins.configureRouting
import net.glazov.plugins.configureSerialization
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    install(Koin) {
        modules(
            databaseModule,
            dataSourcesModule,
            routesModule
        )
    }
    configureSerialization()
    //configureMonitoring()
    configureRouting()
}
