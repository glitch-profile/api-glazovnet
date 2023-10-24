package net.glazov.database

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.CityModel
import org.bson.types.ObjectId

private val mongoUri = ApplicationConfig(null).tryGetString("storage.mongo_db_uri").toString()
private val client = MongoClient.create(mongoUri)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<CityModel>("Cities")

suspend fun getCities(
    filterName: String?
): List<CityModel> {
    val name = filterName ?: ""
    val allCities = collection.find().toList()
    val filteredList = allCities.filter { it.name.contains(name, ignoreCase = true) }
    return filteredList.sortedBy { it.name }
}

suspend fun getCityId(
    cityName: String
): String? {
    val filter = Filters.eq(CityModel::name.name, cityName)
    val city = collection.find(filter).toList().firstOrNull()
    return city?.id ?: addCity(cityName)?.id
}

suspend fun addCity(
    cityName: String
): CityModel? {
    val cityModel = CityModel(
        id = ObjectId().toString(),
        name = cityName
    )
    val status = collection.insertOne(cityModel).wasAcknowledged()
    return if (status) cityModel
    else null
}
