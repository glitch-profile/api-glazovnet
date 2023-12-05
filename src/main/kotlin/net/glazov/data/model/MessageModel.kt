package net.glazov.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MessageModel(
    val senderId: String,
    val senderName: String?,
    val text: String,
    val timestamp: Long
)
