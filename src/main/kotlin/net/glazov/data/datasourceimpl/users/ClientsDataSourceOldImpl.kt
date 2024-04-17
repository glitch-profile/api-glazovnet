package net.glazov.data.datasourceimpl.users

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.AddressesDataSource
import net.glazov.data.datasource.users.ClientsDataSourceOld
import net.glazov.data.datasource.TransactionsDataSource
import net.glazov.data.model.AddressModel
import net.glazov.data.model.ClientModelOld
import net.glazov.data.utils.notificationsmanager.NotificationsTopicsCodes
import net.glazov.data.utils.paymentmanager.ClientNotFoundException
import net.glazov.data.utils.paymentmanager.InsufficientFundsException
import net.glazov.data.utils.paymentmanager.TransactionErrorException
import net.glazov.data.utils.paymentmanager.TransactionManager
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ClientsDataSourceOldImpl(
    db: MongoDatabase,
    private val addresses: AddressesDataSource,
    private val transactions: TransactionsDataSource,
    private val transactionManager: TransactionManager
): ClientsDataSourceOld {

    private val clients = db.getCollection<ClientModelOld>("Clients")

    override suspend fun getAllClients(): List<ClientModelOld> {
        return clients.find().toList().sortedBy { "${it.lastName}${it.firstName}${it.middleName}" }
    }

    override suspend fun createClient(clientModelOld: ClientModelOld): ClientModelOld? {
        val address = addresses.getOrAddAddress(
            city = clientModelOld.address.cityName,
            street = clientModelOld.address.streetName,
            houseNumber = clientModelOld.address.houseNumber
        )
        return if (address != null) {
            val creationDate = LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE)
            val client = clientModelOld.copy(
                id = ObjectId().toString(),
                address = AddressModel(
                    cityName = address.city,
                    streetName = address.street,
                    houseNumber = clientModelOld.address.houseNumber,
                    roomNumber = clientModelOld.address.roomNumber
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

    override suspend fun getClientById(clientId: String): ClientModelOld? {
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

    override suspend fun login(login: String?, password: String?): ClientModelOld? {
        val loginFilter = Filters.eq(ClientModelOld::login.name, login)
        val passwordFilter = Filters.eq(ClientModelOld::password.name, password)
        val filter = Filters.and(loginFilter, passwordFilter)
        return clients.find(filter).singleOrNull()
    }

    override suspend fun addFcmToken(userId: String, newToken: String): Boolean {
        val filter = Filters.eq("_id", userId)
        val update = Updates.addToSet(ClientModelOld::fcmTokensList.name, newToken)
        val result = clients.updateOne(filter, update)
        return result.upsertedId != null
    }

    override suspend fun removeFcmToken(userId: String, tokenToRemove: String): Boolean {
        val filter = Filters.eq("_id", userId)
        val update = Updates.pull(ClientModelOld::fcmTokensList.name, tokenToRemove)
        val result = clients.updateOne(filter, update)
        return result.upsertedId != null
    }

    override suspend fun updateNotificationTopics(clientId: String, newTopicsList: List<String>): Boolean {
        val filter = Filters.eq("_id", clientId)
        val update = Updates.set(ClientModelOld::selectedNotificationsTopics.name, newTopicsList)
        return clients.updateOne(filter, update).upsertedId != null
    }

    override suspend fun updateNotificationsStatus(clientId: String, newStatus: Boolean): Boolean {
        val filter = Filters.eq("_id", clientId)
        val update = Updates.set(ClientModelOld::isNotificationsEnabled.name, newStatus)
        return clients.updateOne(filter, update).upsertedId != null
    }

    override suspend fun getClientsTokensWithSelectedTopic(topic: NotificationsTopicsCodes): List<List<String>> {
        val filter = Filters.and(
            listOf(
                Filters.eq(ClientModelOld::isNotificationsEnabled.name, true),
                Filters.eq(ClientModelOld::selectedNotificationsTopics.name, topic.name)
            )
        )
        return clients.find(filter).toList().mapNotNull {
            it.fcmTokensList
        }
    }

    override suspend fun changeAccountPassword(userId: String, oldPassword: String, newPassword: String): Boolean {
        val filter = Filters.and(
            Filters.eq("_id", userId),
            Filters.eq(ClientModelOld::password.name, oldPassword)
        )
        val update = Updates.set(ClientModelOld::password.name, newPassword)
        val result = clients.updateOne(filter,update)
        return result.upsertedId != null
    }

    override suspend fun changeTariff(userId: String, newTariffId: String): Boolean {
        val filter = Filters.eq("_id", ClientModelOld::tariffId.name)
        val update = Updates.set(ClientModelOld::tariffId.name, newTariffId)
        val result = clients.updateOne(filter, update)
        return result.upsertedId != null
    }

    override suspend fun setIsAccountActive(userId: String, newStatus: Boolean): Boolean {
        val filter = Filters.eq("_id", userId)
        val update = Updates.set(ClientModelOld::isAccountActive.name, newStatus)
        val result = clients.updateOne(filter, update)
        return result.upsertedId != null
    }

    override suspend fun addPositiveTransaction(
        userId: String,
        amount: Double,
        note: String?
    ) {
        val client = getClientById(userId)
        if (client !== null) {
            val transactionResult = transactionManager.makeTransaction()
            if (transactionResult) {
                val newBalance = client.balance + amount
                val filter = Filters.eq("_id", userId)
                val update = Updates.set(ClientModelOld::balance.name, newBalance)
                clients.updateOne(filter, update)
                transactions.addTransaction(
                    clientId = userId,
                    amount = amount,
                    isIncoming = true,
                    note = note
                )
            } else throw TransactionErrorException()
        } else throw ClientNotFoundException()
    }

    override suspend fun addNegativeTransaction(
        userId: String,
        amount: Double,
        note: String?
    ) {
        val client = getClientById(userId)
        if (client !== null) {
            val newBalance = client.balance - amount
            if (newBalance < 0) {
                throw InsufficientFundsException()
            } else {
                val filter = Filters.eq("_id", userId)
                val update = Updates.set(ClientModelOld::balance.name, newBalance)
                clients.updateOne(filter, update)
                transactions.addTransaction(
                    clientId = userId,
                    amount = amount,
                    isIncoming = false,
                    note = note
                )
            }
        } else throw ClientNotFoundException()
    }
}