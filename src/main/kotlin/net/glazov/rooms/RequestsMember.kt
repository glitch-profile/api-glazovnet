package net.glazov.rooms

import io.ktor.websocket.*

data class RequestsMember(
    val memberId: String,
    val socket: WebSocketSession
)
