package net.glazov.database

import com.mongodb.client.model.Filters
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
    val cityName = getCityNameFromDatabaseFormatted(clientModel.address.cityName)
    val streetName = getStreetNameFromDatabaseFormatted(
        cityName = clientModel.address.cityName,
        streetName = clientModel.address.streetName
    )
    return if (cityName != null && streetName != null) {
        val client = clientModel.copy(
            id = ObjectId().toString(),
            address = AddressModel(
                cityName = cityName,
                streetName = streetName.name,
                houseNumber = clientModel.address.houseNumber,
                roomNumber = clientModel.address.roomNumber
            )
        )
        val status = collection.insertOne(client).wasAcknowledged()
        if (status) client else null
    } else {
        null
    }
}

suspend fun login(
    login: String?,
    password: String?
): String? {
    val loginFilter = Filters.eq(ClientModel::login.name, login)
    val passwordFilter = Filters.eq(ClientModel::password.name, password)
    val filter = Filters.and(loginFilter, passwordFilter)
    val client = collection.find(filter).toList().firstOrNull()
    return client?.id
}


