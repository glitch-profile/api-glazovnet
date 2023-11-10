package net.glazov.database

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.StreetModel

private val mongoUri = ApplicationConfig(null).tryGetString("storage.mongo_db_uri").toString()
private val client = MongoClient.create(mongoUri)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<StreetModel>("Streets")

suspend fun getStreets(
    cityName: String?,
    streetName: String?
): List<StreetModel> {
    return if (cityName == null) {
        emptyList()
    } else {
        val city = cityName.lowercase()
        val street = streetName?.lowercase() ?: ""
        val filter = "$city$street"
        val allStreets = collection.find().toList()
        allStreets.filter {
            it.doesMatchFilter(filter)
        }.sortedBy { it.name }
    }
}

suspend fun getStreetNameFromDatabaseFormatted(
    cityName: String,
    streetName: String
): StreetModel? {
    val cityFilter = Filters.eq(StreetModel::city.name, cityName.lowercase())
    val streetFilter = Filters.eq(StreetModel::name.name, streetName.lowercase())
    val filter = Filters.and(cityFilter, streetFilter)
    val street = collection.find(filter).toList().firstOrNull()
    return street ?: addStreet(cityName, streetName)
}

suspend fun addStreet(
    cityName: String,
    streetName: String
): StreetModel? {
    val streetModel = StreetModel(
        city = cityName.lowercase(),
        name = streetName.lowercase()
    )
    val status = collection.insertOne(streetModel).wasAcknowledged()
    return if (status) streetModel else null
}