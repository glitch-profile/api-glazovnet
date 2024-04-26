package net.glazov.data.model.requests

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId

@Serializable
data class SupportRequestModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val creatorPersonId: String,
    val creatorClientId: String,
    val creatorName: String,
    val associatedSupportId: String? = null,
    val title: String,
    val description: String,
    val messages: List<MessageModel> = emptyList(),
    val creationDate: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond(),
    val reopenDate: Long? = null,
    val isNotificationsEnabled: Boolean,
    val status: Int = 0
)
