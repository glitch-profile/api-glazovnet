package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.AddressesDataSource
import net.glazov.data.datasource.ClientsDataSource
import net.glazov.data.datasource.TransactionsDataSource
import net.glazov.data.model.AddressModel
import net.glazov.data.model.AdminModel
import net.glazov.data.model.ClientModel
import net.glazov.data.utils.paymentmanager.ClientNotFoundException
import net.glazov.data.utils.paymentmanager.InsufficientFundsException
import net.glazov.data.utils.paymentmanager.TransactionErrorException
import net.glazov.data.utils.paymentmanager.TransactionManager
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ClientsDataSourceImpl(
    db: MongoDatabase,
    private val addresses: AddressesDataSource,
    private val transactions: TransactionsDataSource,
    private val transactionManager: TransactionManager
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

    override suspend fun updateFcmToken(userId: String, newToken: String?): Boolean {
        val filter = Filters.eq("_id", userId)
        val update = Updates.set(ClientModel::fcmToken.name, newToken)
        val result = clients.updateOne(filter, update)
        return result.upsertedId != null
    }

    override suspend fun updateNotificationTopics(clientId: String, newTopicsList: List<String>): Boolean {
        val filter = Filters.eq("_id", clientId)
        val update = Updates.set(ClientModel::selectedNotificationsTopics.name, newTopicsList)
        return clients.updateOne(filter, update).upsertedId != null
    }

    override suspend fun updateNotificationsStatus(clientId: String, newStatus: Boolean): Boolean {
        val filter = Filters.eq("_id", clientId)
        val update = Updates.set(ClientModel::isNotificationsEnabled.name, newStatus)
        return clients.updateOne(filter, update).upsertedId != null
    }

    override suspend fun changeAccountPassword(userId: String, oldPassword: String, newPassword: String): Boolean {
        val filter = Filters.and(
            Filters.eq("_id", userId),
            Filters.eq(ClientModel::password.name, oldPassword)
        )
        val update = Updates.set(ClientModel::password.name, newPassword)
        val result = clients.updateOne(filter,update)
        return result.upsertedId != null
    }

    override suspend fun changeTariff(userId: String, newTariffId: String): Boolean {
        val filter = Filters.eq("_id", ClientModel::tariffId.name)
        val update = Updates.set(ClientModel::tariffId.name, newTariffId)
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
                val update = Updates.set(ClientModel::balance.name, newBalance)
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
                val update = Updates.set(ClientModel::balance.name, newBalance)
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