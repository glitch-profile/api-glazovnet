package net.glazov.database

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.CityModel
import net.glazov.data.model.StreetModel
import org.bson.types.ObjectId

private val mongoUri = ApplicationConfig(null).tryGetString("storage.mongo_db_uri").toString()
private val client = MongoClient.create(mongoUri)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<StreetModel>("Streets")

suspend fun getStreets(
    filterName: String?
): List<StreetModel> {
    val name = filterName ?: ""
    val allStreets = collection.find().toList()
    val filteredList = allStreets.filter { it.name.contains(name, ignoreCase = true) }
    return filteredList.sortedBy { it.name }
}

suspend fun getStreetNameFromDatabaseFormatted(
    streetName: String
): String? {
    val filter = Filters.eq(CityModel::name.name, streetName.lowercase())
    val street = collection.find(filter).toList().firstOrNull()
    return street?.name ?: addCity(streetName)?.name
}

suspend fun getStreetById(
    id: String
): StreetModel? {
    val filter = Filters.eq("_id", id)
    return collection.find(filter).toList().firstOrNull()
}

suspend fun getStreetId(
    streetName: String
): String? {
    val filter = Filters.eq(StreetModel::name.name, streetName)
    val street = collection.find(filter).toList().firstOrNull()
    return street?.id ?: addStreet(streetName)?.id
}

suspend fun addStreet(
    streetName: String
): StreetModel? {
    val streetModel = StreetModel(
        id = ObjectId().toString(),
        name = streetName.lowercase()
    )
    val status = collection.insertOne(streetModel).wasAcknowledged()
    return if (status) streetModel else null
}