package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class AnnouncementModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val addressFilters: List<List<String>>,
    val title: String,
    val text: String,
    val creationDate: String = OffsetDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME)
) {
    fun isContainingAddress(
        city: String,
        street: String,
        houseNumber: String
    ): Boolean {
        val filterString = "$city,$street,$houseNumber"
        addressFilters.forEach {
            val addressString = it.joinToString(separator = ",")
            if (filterString == addressString) {
                return true
            }
        }
        return false
    }
}
