package net.glazov.database

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.TariffModel
import org.bson.types.ObjectId

private val mongoUri = ApplicationConfig(null).tryGetString("storage.mongo_db_uri").toString()
private val client = MongoClient.create(mongoUri)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<TariffModel>("Tariffs")

suspend fun getAllTariffs() = collection.find().toList().asReversed()

suspend fun addTariff(
    newTariff: TariffModel
): Boolean {
    val tariff = newTariff.copy(
        id = ObjectId.get().toString()
    )
    return collection.insertOne(tariff).wasAcknowledged()
}

suspend fun deleteTariff(
    tariffId: String
): Boolean {
    val filter = Filters.eq("_id", tariffId)
    val tariff = collection.findOneAndDelete(filter)
    return tariff != null
}

suspend fun updateTariff(
    newTariff: TariffModel
): Boolean {
    val filter = Filters.eq("_id", newTariff.id)
    val tariff = collection.findOneAndReplace(filter, newTariff)
    return tariff != null
}
