package net.glazov.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class SimpleResponse<T>(
    val status: Boolean,
    val message: String,
    val data: T
)
