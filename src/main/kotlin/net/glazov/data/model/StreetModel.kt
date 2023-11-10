package net.glazov.data.model

import kotlinx.serialization.Serializable

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
            it.contains(filterString)
        }
    }
}
