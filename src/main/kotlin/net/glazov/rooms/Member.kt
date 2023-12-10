package net.glazov.rooms

import io.ktor.websocket.*

data class Member(
    val memberId: String,
    val sessionId: String,
    val socket: WebSocketSession
)
