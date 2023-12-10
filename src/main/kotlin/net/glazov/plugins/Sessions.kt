package net.glazov.plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import io.ktor.util.*
import net.glazov.sessions.ChatSession

fun Application.configureSessions() {
    install(Sessions) {
        cookie<ChatSession>("SESSION")
    }

//    intercept(ApplicationCallPipeline.Plugins) {
//        if (call.sessions.get<ChatSession>() == null) {
//            val memberId = call.parameters["memberId"]!!
//            call.sessions.set(ChatSession(memberId, generateNonce()))
//        }
//    }
}