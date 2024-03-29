package net.glazov.data.model.requests

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class SupportRequestModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val creatorId: String,
    val creatorName: String,
    val associatedSupportId: String?,
    val title: String,
    val description: String,
    val messages: List<MessageModel> = emptyList(),
    val creationDate: Long = 0,
    val isNotificationsEnabled: Boolean,
    val status: Int = 0
)
