package net.glazov

import io.ktor.server.application.*
import net.glazov.plugins.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    configureKoin()
    configureSerialization()
    //configureMonitoring()
    configureSessions()
    configureSockets()
    configureRouting()
}
