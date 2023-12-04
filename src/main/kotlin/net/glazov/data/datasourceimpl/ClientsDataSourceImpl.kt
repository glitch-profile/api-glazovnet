package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.ClientsDataSource
import net.glazov.data.model.AddressModel
import net.glazov.data.model.ClientModel
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ClientsDataSourceImpl(
    private val db: MongoDatabase,
    private val addresses: AddressesDataSourceImpl
): ClientsDataSource {

    private val clients = db.getCollection<ClientModel>("Clients")

    override suspend fun getAllClients(): List<ClientModel> {
        return clients.find().toList().sortedBy { "${it.lastName}${it.firstName}${it.middleName}" }
    }

    override suspend fun createClient(clientModel: ClientModel): ClientModel? {
        val address = addresses.getOrAddAddress(
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
            val status = clients.insertOne(client).wasAcknowledged()
            if (status) client else null
        } else {
            null
        }
    }

    override suspend fun getClientById(clientId: String): ClientModel? {
        val filter = Filters.eq("_id", clientId)
        return clients.find(filter).toList().firstOrNull()
    }

    override suspend fun login(login: String?, password: String?): String? {
        val loginFilter = Filters.eq(ClientModel::login.name, login)
        val passwordFilter = Filters.eq(ClientModel::password.name, password)
        val filter = Filters.and(loginFilter, passwordFilter)
        val client = clients.find(filter).toList().firstOrNull()
        return client?.id
    }
}