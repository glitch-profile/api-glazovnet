package net.glazov.plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import net.glazov.sessions.ChatSession

fun Application.configureSessions() {
    install(Sessions) {
        cookie<ChatSession>("SESSION")
    }
}