package net.glazov.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseModel(
    val token: String,
    val userId: String,
    val isAdmin: Boolean
)