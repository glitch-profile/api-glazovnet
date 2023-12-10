package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.Instant
import java.util.Date

@Serializable
data class SupportRequestModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val creatorId: String,
    val associatedSupportId: String?,
    val title: String,
    val description: String,
    val messages: List<MessageModel>,
    val creationDate: Long,
    val isSolved: Boolean
)
