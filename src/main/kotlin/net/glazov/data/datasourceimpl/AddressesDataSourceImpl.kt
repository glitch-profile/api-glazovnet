package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.AddressesDataSource
import net.glazov.data.model.RegisteredAddressesModel

class AddressesDataSourceImpl(
    private val db: MongoDatabase
): AddressesDataSource {

    private val addresses = db.getCollection<RegisteredAddressesModel>("Addresses")

    override suspend fun getAddresses(
        city: String,
        street: String,
        isHardSearch: Boolean
    ): List<RegisteredAddressesModel> {
        val cityName = city.lowercase()
        val streetName = street.lowercase()
        val addresses = addresses.find().toList()
        val filteredAddress = if (isHardSearch) {
            addresses.filter { it.doesMatchFilter(cityName, streetName, isHardSearch = true) }
        } else {
            if (city.isNotBlank() && street.isNotBlank())
                addresses.filter { it.doesMatchFilter(cityName, streetName, isHardSearch = false) }
            else addresses
        }
        return filteredAddress.sortedBy { "${it.city}${it.street}" }
    }

    override suspend fun getCitiesNames(city: String): List<String> {
        val cityName = city.lowercase()
        val cities = addresses.distinct<String>(
            RegisteredAddressesModel::city.name
        ).toList()
        return if (cityName.isNotBlank()) {
            cities
                .filter { it.startsWith(cityName) }
                .sortedBy { it }
        } else {
            cities.sortedBy { it }
        }
    }

    override suspend fun getStreetsForCity(city: String, street: String): List<RegisteredAddressesModel> {
        val cityName = city.lowercase()
        val streetName = street.lowercase()
        val filter = Filters.eq(RegisteredAddressesModel::city.name, cityName)
        val streetsList = addresses.find(filter).toList()
        return if (street.isNotBlank()) {
            streetsList
                .filter { it.street.startsWith(streetName) }
                .sortedBy { "${it.city}${it.street}" }
        } else streetsList
            .sortedBy { "${it.city}${it.street}" }
    }

    override suspend fun getOrAddAddress(city: String, street: String, houseNumber: String): RegisteredAddressesModel? {
        val cityName = city.lowercase()
        val streetName = street.lowercase()
        val address = getAddresses(
            cityName,
            streetName,
            isHardSearch = true
        ).firstOrNull()
        return if (address != null) {
            val isHouseNumberFound = address.houseNumbers.any { it == houseNumber }
            if (isHouseNumberFound) {
                address
            } else {
                addHouseNumber(address, houseNumber)
            }
        } else {
            addAddress(
                cityName,
                streetName,
                houseNumber
            )
        }
    }

    override suspend fun isAddressExist(city: String, street: String, houseNumber: String): Boolean {
        val address = getAddresses(city, street, true).firstOrNull()
        return address?.houseNumbers?.any { it == houseNumber } ?: false
    }

    private suspend fun addAddress(city: String, street: String, houseNumber: String): RegisteredAddressesModel? {
        val newAddress = RegisteredAddressesModel(
            city = city,
            street = street,
            houseNumbers = listOf(houseNumber)
        )
        val status = addresses.insertOne(newAddress).wasAcknowledged()
        return if (status) newAddress else null
    }

    private suspend fun addHouseNumber(
        address: RegisteredAddressesModel,
        houseNumber: String
    ): RegisteredAddressesModel? {
        val filter = Filters.eq("_id", address.id)
        val newHouseNumbers = address.houseNumbers.toMutableList()
        newHouseNumbers.add(houseNumber)
        val newAddress = address.copy(
            houseNumbers = newHouseNumbers.sortedBy { numberString ->
                ( numberString.filter { it.isDigit() }).toInt() / 1000f
            }
        )
        return addresses.findOneAndReplace(filter = filter, replacement = newAddress)
    }
}