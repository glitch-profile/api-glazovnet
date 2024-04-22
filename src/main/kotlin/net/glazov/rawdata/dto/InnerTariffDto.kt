package net.glazov.rawdata.dto

import kotlinx.serialization.Serializable

@Serializable
data class InnerTariffDto(
    val id: String,
    val name: String,
    val speed: String,
    val trafficLimit: Long,
    val price: String,
    val specialPrice: String?,
    val active: String,
    val forOrg: String

)
