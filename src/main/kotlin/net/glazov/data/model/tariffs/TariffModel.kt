package net.glazov.data.model.tariffs

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class TariffModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val name: String,
    val description: String?,
    val maxSpeed: Int, // kilobits/second
    val costPerMonth: Int,
    val prepaidTraffic: Long?, // kilobytes
    val prepaidTrafficDescription: String?,
    val isActive: Boolean = true,
    val isForOrganization: Boolean = false
)