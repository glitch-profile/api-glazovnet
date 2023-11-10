package net.glazov.database

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.CityModel

private val mongoUri = ApplicationConfig(null).tryGetString("storage.mongo_db_uri").toString()
private val client = MongoClient.create(mongoUri)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<CityModel>("Cities")

suspend fun getCities(
    cityName: String?
): List<CityModel> {
    val name = cityName?.lowercase() ?: ""
    val allCities = collection.find().toList()
    val filteredList = allCities.filter { it.name.contains(name) }
    return filteredList.sortedBy { it.name }
}

suspend fun getCityNameFromDatabaseFormatted(
    cityName: String
): String? {
    val filter = Filters.eq(CityModel::name.name, cityName.lowercase())
    val city = collection.find(filter).toList().firstOrNull()
    return city?.name ?: addCity(cityName)?.name
}

suspend fun addCity(
    cityName: String
): CityModel? {
    val cityModel = CityModel(
        name = cityName.lowercase()
    )
    val status = collection.insertOne(cityModel).wasAcknowledged()
    return if (status) cityModel
    else null
}
