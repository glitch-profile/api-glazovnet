package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class CityModel(
    @BsonId
    val cityId: String = ObjectId().toString(),
    val name: String
)
