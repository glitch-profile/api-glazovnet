package net.glazov.database

import com.mongodb.client.model.Filters
import net.glazov.data.model.TariffModel
import org.bson.types.ObjectId
import org.litote.kmongo.KMongo
import org.litote.kmongo.deleteOne
import org.litote.kmongo.getCollection

private val client = KMongo.createClient()
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<TariffModel>("Tariffs")

suspend fun getAllTariffs(status: Boolean? = null): List<TariffModel?> {
    return if (status !== null) {
        val filter = Filters.eq(TariffModel::isActive.name, status)
        collection.find(filter).toList().asReversed()
    } else {
        collection.find().toList().asReversed()
    }
}

suspend fun addTariff(
    newTariff: TariffModel
): Boolean {
    val tariff = newTariff.copy(
        id = ObjectId().toString()
    )
    return collection.insertOne(tariff).wasAcknowledged()
}

suspend fun deleteTariff(
    tariffId: String
): Boolean {
    val filter = Filters.eq("_id", tariffId)
    val tariff = collection.findOneAndDelete(filter)
    return tariff !== null
}