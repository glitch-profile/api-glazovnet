package net.glazov.database

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.AddressModel
import net.glazov.data.model.ClientModel
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val mongoUri = ApplicationConfig(null).tryGetString("storage.mongo_db_uri").toString()
private val client = MongoClient.create(mongoUri)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<ClientModel>("Clients")

suspend fun getAllClients() = collection.find().toList().sortedBy { it.lastName }

suspend fun createClient(
    clientModel: ClientModel
): ClientModel? {
    val address = getOrAddAddress(
        city = clientModel.address.cityName,
        street = clientModel.address.streetName,
        houseNumber = clientModel.address.houseNumber
    )
    return if (address != null) {
        val creationDate = LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE)
        val client = clientModel.copy(
            id = ObjectId().toString(),
            address = AddressModel(
                cityName = address.city,
                streetName = address.street,
                houseNumber = clientModel.address.houseNumber,
                roomNumber = clientModel.address.roomNumber
            ),
            accountCreationDate = creationDate,
            debitDate = creationDate
            //TODO:Add debit date calculation
        )
        val status = collection.insertOne(client).wasAcknowledged()
        if (status) client else null
    } else {
        null
    }
}

suspend fun getClientById(
    clientId: String
): ClientModel? {
    val filter = Filters.eq("_id", clientId)
    return collection.find(filter).toList().firstOrNull()
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




