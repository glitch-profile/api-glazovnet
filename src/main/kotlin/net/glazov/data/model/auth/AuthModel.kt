package net.glazov.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthModel(
    val username: String,
    val password: String
)
