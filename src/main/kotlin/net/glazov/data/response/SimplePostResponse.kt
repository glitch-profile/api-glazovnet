package net.glazov.data.response

import kotlinx.serialization.Serializable
import net.glazov.data.model.PostModel

@Serializable
data class SimplePostResponse(
    val status: Boolean,
    val message: String,
    val data: List<PostModel>
)