package net.glazov.database

import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.AddressModel
import net.glazov.data.model.ClientModel
import org.bson.types.ObjectId

private val mongoUri = ApplicationConfig(null).tryGetString("storage.mongo_db_uri").toString()
private val client = MongoClient.create(mongoUri)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<ClientModel>("Clients")

suspend fun getAllClients() = collection.find().toList().sortedBy { it.firstName }

suspend fun createClient(
    clientModel: ClientModel
): ClientModel? {
    val client = clientModel.copy(
        id = ObjectId().toString()
    )
    val cityId = getCityId(client.address.cityId)
    val streetId = getStreetId(client.address.streetId)
    return if (cityId != null && streetId != null) {
        val clientModelToInsert = client.copy(
            address = AddressModel(
                cityId = cityId,
                streetId = streetId,
                houseNumber = client.address.houseNumber,
                roomNumber = client.address.roomNumber
            )
        )
        val status = collection.insertOne(clientModelToInsert).wasAcknowledged()
        if (status) client else null
    } else {
        null
    }

}


