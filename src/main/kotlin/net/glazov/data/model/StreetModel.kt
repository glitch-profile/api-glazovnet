package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class StreetModel(
    val city: String,
    val name: String
) {
    fun doesMatchFilter(
        filterString: String
    ): Boolean {
        val matchingCombinations = listOf<String>(
            "$city$name",
            "$city $name",
            "$name$city",
            "$name $city",
            "$name, $city",
            "${city.first()}$name",
            "${city.first()} $name",
            "${name.first()}$city",
            "${name.first()} $city",
        )
        return matchingCombinations.any {
            it.contains(filterString, ignoreCase = true)
        }
    }
}
