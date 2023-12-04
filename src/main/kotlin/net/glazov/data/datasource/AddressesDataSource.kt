package net.glazov.data.datasource

import net.glazov.data.model.RegisteredAddressesModel

interface AddressesDataSource {

    suspend fun getAddresses(
        city: String,
        street: String,
        isHardSearch: Boolean = false
    ): List<RegisteredAddressesModel>

    suspend fun getCitiesNames(
        city: String = ""
    ): List<String>

    suspend fun getStreetsForCity(
        city: String,
        street: String = ""
    ): List<RegisteredAddressesModel>

    suspend fun getOrAddAddress(
        city: String,
        street: String,
        houseNumber: String
    ): RegisteredAddressesModel?

    suspend fun isAddressExist(
        city: String,
        street: String,
        houseNumber: String
    ): Boolean

    suspend fun addAddress(
        city: String,
        street: String,
        houseNumber: String
    ): RegisteredAddressesModel?

    suspend fun addHouseNumber(
        address: RegisteredAddressesModel,
        houseNumber: String
    ): RegisteredAddressesModel?

}