package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class FilterModel(
    val id: String = ObjectId().toString(),
    val name: String,
    val addressFilters: List<List<String>>
)
 //элементы фильтра в списке - город, улица, дом