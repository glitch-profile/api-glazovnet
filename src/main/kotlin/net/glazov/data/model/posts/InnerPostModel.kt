package net.glazov.data.model.posts

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId

@Serializable
data class InnerPostModel(
    val id: String = ObjectId().toString(),
    val title: String?,
    val text: String,
    val creationDate: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
)