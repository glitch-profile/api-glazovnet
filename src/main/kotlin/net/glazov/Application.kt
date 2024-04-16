package net.glazov

import io.ktor.server.application.*
import net.glazov.plugins.*
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    configureKoin()
    configureAuthentication()
    configureSerialization()
    //configureMonitoring()
    configureSessions()
    configureSockets()
    configureRouting()
    configureFirebase()
}
