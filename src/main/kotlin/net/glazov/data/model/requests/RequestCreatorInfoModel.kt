package net.glazov.data.model.requests

import kotlinx.serialization.Serializable

@Serializable
data class RequestCreatorInfoModel(
    val accountNumber: String,
    val fullName: String
)