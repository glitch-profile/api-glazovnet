package net.glazov.data.model.posts

data class InnerPostModel(
    val id: String,
    val title: String?,
    val text: String,
    val creationDate: Long
)