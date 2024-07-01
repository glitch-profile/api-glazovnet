package net.glazov.data.model.requests

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    val associatedSupportId: String? = null,
    val title: String,
    val description: String,
    @Transient
    val messages: List<MessageModel> = emptyList(),
    val creationDate: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond(),
    val reopenDate: Long? = null,
    val isNotificationsEnabled: Boolean,
    val status: Int = 0
)
