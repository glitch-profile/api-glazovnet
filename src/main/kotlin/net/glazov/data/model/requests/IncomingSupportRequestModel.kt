package net.glazov.data.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class IncomingSupportRequestModel(
    val creatorClientId: String,
    val title: String,
    val description: String,
    val isNotificationsEnabled: Boolean
)