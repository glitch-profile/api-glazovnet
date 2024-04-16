package net.glazov.plugins

import io.ktor.server.application.*
import io.ktor.server.sessions.*
import net.glazov.sessions.OAuthSession

fun Application.configureSessions() {
    install(Sessions) {
        cookie<OAuthSession>("oauth_session")
    }
}