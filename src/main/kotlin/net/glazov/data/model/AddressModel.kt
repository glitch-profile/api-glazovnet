package net.glazov.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AddressModel(
    val cityName: String,
    val streetName: String,
    val houseNumber: Int,
    val roomNumber: Int
)
