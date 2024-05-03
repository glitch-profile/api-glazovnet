package net.glazov.data.model.posts

import kotlinx.serialization.Serializable

@Serializable
data class IncomingInnerPostData(
    val title: String?,
    val text: String
)
