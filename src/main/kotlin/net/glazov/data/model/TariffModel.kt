package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class TariffModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String,
    val description: String?,
    val categoryCode: Int,
    val maxSpeed: Int,
    val costPerMonth: Int,
    val prepaidTraffic: Int?,
    val prepaidTrafficDescription: String?
)
