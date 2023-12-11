package net.glazov

import io.ktor.server.application.*
import net.glazov.plugins.configureKoin
import net.glazov.plugins.configureRouting
import net.glazov.plugins.configureSerialization
import net.glazov.plugins.configureSockets

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    configureKoin()
    configureSerialization()
    //configureMonitoring()
    configureSockets()
    configureRouting()
}
