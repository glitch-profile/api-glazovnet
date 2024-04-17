package net.glazov.data.datasourceimpl.users

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import net.glazov.data.datasource.AddressesDataSource
import net.glazov.data.datasource.TransactionsDataSource
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.model.AddressModel
import net.glazov.data.model.ClientModelOld
import net.glazov.data.model.users.ClientModel
import net.glazov.data.utils.paymentmanager.ClientNotFoundException
import net.glazov.data.utils.paymentmanager.InsufficientFundsException
import net.glazov.data.utils.paymentmanager.TransactionErrorException
import net.glazov.data.utils.paymentmanager.TransactionManager

class ClientsDataSourceImpl(
    db: MongoDatabase,
    private val personsDataSource: PersonsDataSource,
    private val addressesDataSource: AddressesDataSource,
    private val transactions: TransactionsDataSource,
    private val transactionManager: TransactionManager
): ClientsDataSource {

    val clients = db.getCollection<ClientModel>("Clients V2")

    override suspend fun getClientById(clientId: String): ClientModel? {
        val filter = Filters.eq("_id", clientId)
        return clients.find(filter).singleOrNull()
    }

    override suspend fun addClient(
        associatedPersonId: String,
        accountNumber: String,
        address: AddressModel
    ): ClientModel? {
        val clientAddress = addressesDataSource.getOrAddAddress(
            city = address.cityName,
            street = address.streetName,
            houseNumber = address.cityName
        )
        return if (clientAddress != null) {
            val client = ClientModel(
                personId = associatedPersonId,
                accountNumber = accountNumber,
                address = AddressModel(
                    cityName = clientAddress.city,
                    streetName = clientAddress.street,
                    houseNumber = address.houseNumber,
                    roomNumber = address.roomNumber
                )
            )
            val status = clients.insertOne(client)
            if (status.insertedId != null) client else null
        } else null
    }

    override suspend fun changeTariff(clientId: String, newTariffId: String): Boolean {
        val filter = Filters.eq("_id", clientId)
        val update = Updates.set(ClientModel::tariffId.name, newTariffId)
        val result = clients.updateOne(filter, update)
        return result.upsertedId != null
    }

    override suspend fun setIsAccountActive(clientId: String, newStatus: Boolean): Boolean {
        val filter = Filters.eq("_id", clientId)
        val update = Updates.set(ClientModel::isAccountActive.name, newStatus)
        val result = clients.updateOne(filter, update)
        return result.upsertedId != null
    }

    override suspend fun addPositiveTransaction(clientId: String, amount: Double, note: String?) {
        val client = getClientById(clientId)
        if (client !== null) {
            val transactionResult = transactionManager.makeTransaction()
            if (transactionResult) {
                val newBalance = client.balance + amount
                val filter = Filters.eq("_id", clientId)
                val update = Updates.set(ClientModelOld::balance.name, newBalance)
                clients.updateOne(filter, update)
                transactions.addTransaction(
                    clientId = clientId,
                    amount = amount,
                    isIncoming = true,
                    note = note
                )
            } else throw TransactionErrorException()
        } else throw ClientNotFoundException()
    }

    override suspend fun addNegativeTransaction(clientId: String, amount: Double, note: String?) {
        val client = getClientById(clientId)
        if (client !== null) {
            val newBalance = client.balance - amount
            if (newBalance < 0) {
                throw InsufficientFundsException()
            } else {
                val filter = Filters.eq("_id", clientId)
                val update = Updates.set(ClientModelOld::balance.name, newBalance)
                clients.updateOne(filter, update)
                transactions.addTransaction(
                    clientId = clientId,
                    amount = amount,
                    isIncoming = false,
                    note = note
                )
            }
        } else throw ClientNotFoundException()
    }
}