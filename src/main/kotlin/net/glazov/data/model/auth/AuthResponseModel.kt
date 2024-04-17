package net.glazov.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseModel(
    val token: String,
    val personId: String,
    val clientId: String?,
    val employeeId: String?
)