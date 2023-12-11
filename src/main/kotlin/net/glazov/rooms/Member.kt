package net.glazov.rooms

import io.ktor.websocket.*

data class Member(
    val memberId: String,
    val socket: WebSocketSession
)
