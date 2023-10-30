package net.glazov.routes

import kotlinx.serialization.Serializable
import net.glazov.data.model.FilterModel

@Serializable
data class FilterResponse(
    val status: Boolean,
    val message: String,
    val data: List<FilterModel>
)
