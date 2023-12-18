package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MessageModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val senderId: String,
    val text: String,
    val timestamp: Long
)
