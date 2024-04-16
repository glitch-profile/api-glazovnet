package net.glazov.data.model.posts

import kotlinx.serialization.Serializable

@Serializable
data class InnerPostModel(
    val id: String,
    val title: String?,
    val text: String,
    val creationDate: Long
)