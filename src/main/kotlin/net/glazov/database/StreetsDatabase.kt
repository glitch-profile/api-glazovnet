package net.glazov.database

import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.CityModel
import net.glazov.data.model.StreetModel

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