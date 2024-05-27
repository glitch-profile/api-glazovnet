package net.glazov.data.datasourceimpl.users

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.AddressesDataSource
import net.glazov.data.datasource.TransactionsDataSource
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.model.AddressModel
import net.glazov.data.model.users.ClientModel
import net.glazov.data.model.users.PersonModel
import net.glazov.data.utils.paymentmanager.ClientNotFoundException
import net.glazov.data.utils.paymentmanager.InsufficientFundsException
import net.glazov.data.utils.paymentmanager.TransactionErrorException
import net.glazov.data.utils.paymentmanager.TransactionManager
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class ClientsDataSourceImpl(
    db: MongoDatabase,
    private val persons: PersonsDataSource,
    private val addresses: AddressesDataSource,
    private val transactions: TransactionsDataSource,
    private val transactionManager: TransactionManager
): ClientsDataSource {

    private val clients = db.getCollection<ClientModel>("Clients")

    override suspend fun getAllClients(): List<ClientModel> {
        return clients.find().toList()
    }

    override suspend fun getClientById(clientId: String): ClientModel? {
        val filter = Filters.eq("_id", clientId)
        return clients.find(filter).singleOrNull()
    }

    override suspend fun getAssociatedPerson(clientId: String): PersonModel? {
        val client = getClientById(clientId)
        return if (client != null) {
            persons.getPersonById(client.personId)
        } else null
    }

    override suspend fun getClientByPersonId(personId: String): ClientModel? {
        val filter = Filters.eq(ClientModel::personId.name, personId)
        return clients.find(filter).singleOrNull()
    }

    override suspend fun addClient(
        associatedPersonId: String,
        accountNumber: String,
        tariffId: String,
        address: AddressModel
    ): ClientModel? {
        val isPersonAvailable = getClientByPersonId(associatedPersonId) == null
        return if (isPersonAvailable) {
            val clientAddress = addresses.getOrAddAddress(
                city = address.cityName,
                street = address.streetName,
                houseNumber = address.houseNumber
            )
            if (clientAddress != null) {
                val client = ClientModel(
                    personId = associatedPersonId,
                    accountNumber = accountNumber,
                    tariffId = tariffId,
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
        } else null
    }

    override suspend fun changeTariff(clientId: String, newTariffId: String?): Boolean {
        val filter = Filters.eq("_id", clientId)
        val update = Updates.set(ClientModel::pendingTariffId.name, newTariffId)
        val result = clients.updateOne(filter, update)
        return result.modifiedCount != 0L
    }

    override suspend fun blockClientAccount(clientId: String): Boolean {
        val filter = Filters.eq("_id", clientId)
        val currentTimestamp = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
        val update = Updates.combine(
            Updates.set(ClientModel::isAccountActive.name, true),
            Updates.set(ClientModel::accountLockTimestamp.name, currentTimestamp)
        )
        val result = clients.updateOne(filter, update)
        return result.modifiedCount != 0L
    }

    override suspend fun unblockClientAccount(clientId: String): Boolean {
        val filter = Filters.eq("_id", clientId)
        val client = clients.find(filter).singleOrNull() ?: return false
        if (!client.isAccountActive && client.balance > 0) {
            val currentTimestamp = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
            val lastBlockTimestamp = client.accountLockTimestamp ?: currentTimestamp
            val lockDuration = Duration.ofSeconds(currentTimestamp - lastBlockTimestamp).toDays()
            val update = if (lockDuration == 0L) {
                Updates.set(ClientModel::isAccountActive.name, true)
            } else {
                val newClientDebitDate = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(client.debitDate), ZoneId.systemDefault()
                ).plusDays(lockDuration)
                Updates.combine(
                    Updates.set(ClientModel::isAccountActive.name, true),
                    Updates.set(ClientModel::debitDate.name, newClientDebitDate)
                )
            }
            val result = clients.updateOne(filter, update)
            return result.modifiedCount != 0L
        } else return false
    }

    override suspend fun addPositiveTransaction(clientId: String, amount: Float, note: String?) {
        val client = getClientById(clientId)
        if (client !== null) {
            val transactionResult = transactionManager.makeTransaction()
            if (transactionResult) {
                val newBalance = client.balance + amount
                val filter = Filters.eq("_id", clientId)
                val update = Updates.set(ClientModel::balance.name, newBalance)
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

    override suspend fun addNegativeTransaction(clientId: String, amount: Float, note: String?) {
        val client = getClientById(clientId)
        if (client !== null) {
            val newBalance = client.balance - amount
            if (newBalance < 0) {
                throw InsufficientFundsException()
            } else {
                val filter = Filters.eq("_id", clientId)
                val update = Updates.set(ClientModel::balance.name, newBalance)
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

    override suspend fun connectService(clientId: String, serviceId: String): Boolean {
        val filter = Filters.eq("_id", clientId)
        val update = Updates.addToSet(ClientModel::connectedServices.name, serviceId)
        val result = clients.updateOne(filter, update)
        return result.modifiedCount != 0L
    }

    override suspend fun disconnectService(clientId: String, serviceId: String): Boolean {
        val filter = Filters.eq("_id", clientId)
        val update = Updates.pull(ClientModel::connectedServices.name, serviceId)
        val result = clients.updateOne(filter, update)
        return result.modifiedCount != 0L
    }

    // FOR MONTHLY PAYMENTS

    override suspend fun getClientsForBillingDate(dateSeconds: Long): List<ClientModel> {
        val filter = Filters.and(
            Filters.lte(ClientModel::debitDate.name, dateSeconds),
            Filters.eq(ClientModel::isAccountActive.name, true)
        )
        return clients.find(filter).toList()
    }

    override suspend fun initStartOfBillingMonth(
        clientId: String,
        nextBillingDate: Long,
        paymentAmount: Int
    ) {
        val filter = Filters.eq("_id", clientId)
        val client = clients.find(filter).singleOrNull() ?: return
        val newBalance = client.balance - paymentAmount
        var update = Updates.combine(
            Updates.set(ClientModel::balance.name, newBalance),
            Updates.set(ClientModel::debitDate.name, nextBillingDate)
        )
        if (newBalance < 0) blockClientAccount(clientId)
        if (client.pendingTariffId != null) {
            update = Updates.combine(
                update,
                Updates.set(ClientModel::tariffId.name, client.pendingTariffId),
                Updates.set(ClientModel::pendingTariffId.name, null)
            )
        }
        clients.updateOne(filter, update)
    }
}