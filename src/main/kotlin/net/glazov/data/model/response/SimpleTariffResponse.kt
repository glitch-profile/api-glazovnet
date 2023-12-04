package net.glazov.data.model.response

import kotlinx.serialization.Serializable
import net.glazov.data.model.TariffModel

@Serializable
data class SimpleTariffResponse(
    val status: Boolean,
    val message: String,
    val data: List<TariffModel>
)