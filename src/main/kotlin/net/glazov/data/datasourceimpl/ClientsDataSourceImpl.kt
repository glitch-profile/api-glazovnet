package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.AddressesDataSource
import net.glazov.data.datasource.ClientsDataSource
import net.glazov.data.model.AddressModel
import net.glazov.data.model.ClientModel
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ClientsDataSourceImpl(
    private val db: MongoDatabase,
    private val addresses: AddressesDataSource
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

    override suspend fun getClientNameById(clientId: String, useShortForm: Boolean): String {
        val client = getClientById(clientId)
        return if (client != null) {
            if (useShortForm) "${client.firstName} ${client.middleName}"
            else "${client.lastName} ${client.firstName} ${client.middleName}"
        } else "Unknown client"
    }

    override suspend fun login(login: String?, password: String?): ClientModel? {
        val loginFilter = Filters.eq(ClientModel::login.name, login)
        val passwordFilter = Filters.eq(ClientModel::password.name, password)
        val filter = Filters.and(loginFilter, passwordFilter)
        return clients.find(filter).singleOrNull()
    }
}