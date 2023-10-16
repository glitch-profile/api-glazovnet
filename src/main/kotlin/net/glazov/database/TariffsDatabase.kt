package net.glazov.database

import com.mongodb.client.model.Filters
import net.glazov.data.model.TariffModel
import org.bson.types.ObjectId
import org.litote.kmongo.*

private const val connectionString = "mongodb+srv://korablev2002:ik130702ik@cluster0.macg7bc.mongodb.net/?retryWrites=true&w=majority"
private val client = KMongo.createClient(connectionString)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<TariffModel>("Tariffs")

suspend fun getAllTariffs(): List<TariffModel?> {
    return collection.find().toList().asReversed()
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

suspend fun updateTariff(
    newTariff: TariffModel
): Boolean {
    return collection.findOneById(newTariff.id)?.let {
        collection.updateOneById(id = it.id, update = newTariff).wasAcknowledged()
    } ?: false
}