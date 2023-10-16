package net.glazov

import io.ktor.server.application.*
import net.glazov.database.getKey
import net.glazov.plugins.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    configureSerialization()
    //configureMonitoring()
    configureRouting()
}
