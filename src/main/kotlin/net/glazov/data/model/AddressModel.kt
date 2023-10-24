package net.glazov.data.model

import kotlinx.serialization.Serializable

@Serializable
data class AddressModel(
    val cityId: String, //ID города (деревни) с тааблицы cities
    val streetId: String, //ID улицы с таблицы streets
    val houseNumber: Int,
    val roomNumber: Int
)
