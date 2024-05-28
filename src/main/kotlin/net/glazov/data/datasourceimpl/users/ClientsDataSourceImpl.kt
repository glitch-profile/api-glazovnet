package net.glazov.data.datasourceimpl.users

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.AddressesDataSource
import net.glazov.data.datasource.ServicesDataSource
import net.glazov.data.datasource.TariffsDataSource
import net.glazov.data.datasource.TransactionsDataSource
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.model.AddressModel
import net.glazov.data.model.users.ClientModel
import net.glazov.data.model.users.PersonModel
import net.glazov.data.utils.paymentmanager.*
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

class ClientsDataSourceImpl(
    db: MongoDatabase,
    private val persons: PersonsDataSource,
    private val addresses: AddressesDataSource,
    private val tariffs: TariffsDataSource,
    private val services: ServicesDataSource,
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
                val tariff = tariffs.getTariffById(tariffId) ?: return null
                val client = ClientModel(
                    personId = associatedPersonId,
                    accountNumber = accountNumber,
                    tariffId = tariffId,
                    minRequiredBalance = tariff.costPerMonth,
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
        if (!client.isAccountActive) {
            val currentTimestamp = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
            val lastBlockTimestamp = client.accountLockTimestamp ?: currentTimestamp
            val lockDuration = Duration.ofSeconds(currentTimestamp - lastBlockTimestamp).toDays()
            val update = if (lockDuration == 0L) {
                Updates.combine(
                    Updates.set(ClientModel::isAccountActive.name, true),
                    Updates.set(ClientModel::accountLockTimestamp.name, null)
                )
            } else {
                val newClientDebitDate = OffsetDateTime.ofInstant(
                    Instant.ofEpochSecond(client.debitDate), ZoneId.systemDefault()
                ).plusDays(lockDuration)
                Updates.combine(
                    Updates.set(ClientModel::isAccountActive.name, true),
                    Updates.set(ClientModel::accountLockTimestamp.name, null),
                    Updates.set(ClientModel::debitDate.name, newClientDebitDate)
                )
            }
            val result = clients.updateOne(filter, update)
            return result.modifiedCount != 0L
        } else return false
    }

    override suspend fun addPositiveTransaction(clientId: String, amount: Float, note: TransactionNoteTextCode?) {
        getClientById(clientId) ?: kotlin.run {
            throw ClientNotFoundException()
        }
        val isTransactionSuccessful = transactionManager.makeTransaction()
        if (isTransactionSuccessful) {
            val filter = Filters.eq("_id", clientId)
            val update = Updates.inc(ClientModel::balance.name, amount)
            clients.updateOne(filter, update)
            transactions.addTransaction(
                clientId = clientId,
                amount = amount,
                isIncoming = true,
                note = note
            )
        } else throw TransactionErrorException()
    }

    override suspend fun addSoftNegativeTransaction(clientId: String, amount: Float, note: TransactionNoteTextCode?) {
        val filter = Filters.eq("_id", clientId)
        val update = Updates.inc(ClientModel::balance.name, -amount)
        clients.updateOne(filter, update)
        transactions.addTransaction(
            clientId = clientId,
            amount = amount,
            isIncoming = false,
            note = note
        )
    }

    override suspend fun addStrictNegativeTransaction(clientId: String, amount: Float, note: TransactionNoteTextCode?) {
        val client = getClientById(clientId) ?: kotlin.run {
            throw ClientNotFoundException()
        }
        val newClientBalance = client.balance - amount
        if (newClientBalance >= 0) {
            val filter = Filters.eq("_id", clientId)
            val update = Updates.set(ClientModel::balance.name, newClientBalance)
            clients.updateOne(filter, update)
            transactions.addTransaction(
                clientId = clientId,
                amount = amount,
                isIncoming = false,
                note = note
            )
        } else throw InsufficientFundsException()
    }

    override suspend fun connectService(clientId: String, serviceId: String): Boolean {
        val service = services.getServiceById(serviceId) ?: return false
        if (service.connectionCost != null) {
            addStrictNegativeTransaction(
                clientId = clientId,
                amount = service.connectionCost.toFloat(),
                note = TransactionNoteTextCode.ConnectingNewService
            )
        }
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

    override suspend fun getClientsForBillingDate(
        currentDateTimestamp: Long,
        minLockDateTimestamp: Long
    ): List<ClientModel> {
        val filter = Filters.and(
            Filters.lte(ClientModel::debitDate.name, currentDateTimestamp),
            Filters.or(
                Filters.eq(ClientModel::accountLockTimestamp.name, null),
                Filters.gte(ClientModel::accountLockTimestamp.name, minLockDateTimestamp)
            )
        )
        return clients.find(filter).toList()
    }

    override suspend fun closeBillingMonth(
        clientId: String,
        nextBillingDate: Long,
        paymentAmount: Int
    ) {
        addSoftNegativeTransaction(
            clientId = clientId,
            amount = paymentAmount.toFloat(),
            note = TransactionNoteTextCode.MonthlyPayment
        )
        val filter = Filters.eq("_id", clientId)
        val update = Updates.set(ClientModel::debitDate.name, nextBillingDate)
        clients.updateOne(filter, update)
    }

    override suspend fun connectPendingTariff(clientId: String): Boolean {
        val filter = Filters.eq("_id", clientId)
        val client = clients.find(filter).singleOrNull() ?: return false
        return if (client.pendingTariffId != null) {
            val newTariff = tariffs.getTariffById(client.pendingTariffId) ?: return false
            val update = Updates.combine(
                Updates.set(ClientModel::tariffId.name, client.pendingTariffId),
                Updates.set(ClientModel::pendingTariffId.name, null),
                Updates.set(ClientModel::minRequiredBalance.name, newTariff.costPerMonth)
            )
            val result = clients.updateOne(filter, update)
            result.modifiedCount != 0L
        } else false
    }
}