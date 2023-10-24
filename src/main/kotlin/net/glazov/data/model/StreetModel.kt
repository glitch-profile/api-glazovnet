package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class StreetModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String
)