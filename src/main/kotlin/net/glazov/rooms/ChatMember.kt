package net.glazov.rooms

import io.ktor.websocket.*

data class ChatMember(
    val memberId: String,
    val memberName: String,
    val isAdmin: Boolean,
    val socket: WebSocketSession
)
