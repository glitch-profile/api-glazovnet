package net.glazov.data.model.posts

import kotlinx.serialization.Serializable
import net.glazov.data.model.ImageModel

@Serializable
data class IncomingPostModel(
    val id: String?,
    val title: String,
    val text: String,
    val image: ImageModel?
)
