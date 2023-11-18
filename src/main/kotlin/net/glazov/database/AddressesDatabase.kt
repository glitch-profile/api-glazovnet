package net.glazov.database

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.RegisteredAddressesModel

private val mongoUri = ApplicationConfig(null).tryGetString("storage.mongo_db_uri").toString()
private val client = MongoClient.create(mongoUri)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<RegisteredAddressesModel>("Addresses")

suspend fun getAddresses(
    city: String,
    street: String,
    isHardSearch: Boolean = false
): List<RegisteredAddressesModel> {
    val cityName = city.lowercase()
    val streetName = street.lowercase()
    val addresses = collection.find().toList()
    val filteredAddress = addresses.filter { it.doesMatchFilter(cityName, streetName, isHardSearch) }
    return filteredAddress.sortedBy { "${it.city}${it.street}" }
}

suspend fun getCitiesNames(
    city: String = ""
): List<String> {
    val cityName = city.lowercase()
    val cities = collection.distinct<String>(
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

suspend fun getStreetsForCity(
    city: String,
    street: String = ""
): List<RegisteredAddressesModel> {
    val cityName = city.lowercase()
    val streetName = street.lowercase()
    val filter = Filters.eq(RegisteredAddressesModel::city.name, cityName)
    val streetsList = collection.find(filter).toList()
    return streetsList
        .filter { it.street.startsWith(streetName) }
        .sortedBy { "${it.city}${it.street}" }
}

suspend fun getOrAddAddress(
    city: String,
    street: String,
    houseNumber: String
): RegisteredAddressesModel? {
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

suspend fun isAddressExist(
    city: String,
    street: String,
    houseNumber: String
): Boolean {
    val address = getAddresses(city, street, true).firstOrNull()
    return address?.houseNumbers?.any { it == houseNumber } ?: false
}

private suspend fun addAddress(
    city: String,
    street: String,
    houseNumber: String
): RegisteredAddressesModel? {
    val newAddress = RegisteredAddressesModel(
        city = city,
        street = street,
        houseNumbers = listOf(houseNumber)
    )
    val status = collection.insertOne(newAddress).wasAcknowledged()
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
    return collection.findOneAndReplace(filter = filter, replacement = newAddress)
}


