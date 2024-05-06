package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class ServiceModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String,
    val nameEn: String,
    val description: String,
    val descriptionEn: String,
    val costPerMonth: Int,
    val connectionCost: Int?,
    val isActive: Boolean
)
