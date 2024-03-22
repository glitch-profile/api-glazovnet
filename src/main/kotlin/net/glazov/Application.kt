package net.glazov

import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.glazov.plugins.*

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
