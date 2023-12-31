package net.glazov.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageModel(
    val imageUrl: String,
    val imageWidth: Int,
    val imageHeight: Int,
)
