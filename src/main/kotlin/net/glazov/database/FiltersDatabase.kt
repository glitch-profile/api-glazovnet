package net.glazov.database

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.FilterModel
import org.bson.types.ObjectId

private val mongoUri = ApplicationConfig(null).tryGetString("storage.mongo_db_uri").toString()
private val client = MongoClient.create(mongoUri)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<FilterModel>("Filters")

suspend fun getAllFilters(
    limit: Int? = null
): List<FilterModel> {
    val filtersList = collection.find().toList().reversed()
    return if (limit != null) {
        if (limit > 0) filtersList.take(limit)
        else emptyList()
    } else {
        filtersList
    }
}

suspend fun addFilter(
    filter: FilterModel
): FilterModel? {
    val filterToInsert = filter.copy(
        id = ObjectId().toString(),
        addressFilters = filter.addressFilters.map { it.take(3) }
    )
    val status = collection.insertOne(filterToInsert).wasAcknowledged()
    return if (status) filterToInsert
    else null
}

suspend fun deleteFilter(
    filterId: String
): Boolean {
    val filter = Filters.eq("_id", filterId)
    val deletedFilter = collection.findOneAndDelete(filter)
    return deletedFilter != null
}

suspend fun getFiltersWithName(
    filterName: String
): List<FilterModel> {
    val filter = Filters.eq(FilterModel::name.name, filterName)
    return collection.find(filter).toList().asReversed()
}