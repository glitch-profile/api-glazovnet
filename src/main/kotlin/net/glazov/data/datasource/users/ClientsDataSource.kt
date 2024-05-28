package net.glazov.data.datasource.users

import net.glazov.data.model.AddressModel
import net.glazov.data.model.users.ClientModel
import net.glazov.data.model.users.PersonModel
import net.glazov.data.utils.paymentmanager.TransactionNoteTextCode

interface ClientsDataSource {

    suspend fun getAllClients(): List<ClientModel>

    suspend fun getClientById(clientId: String): ClientModel?

    suspend fun getClientByPersonId(personId: String): ClientModel?

    suspend fun getAssociatedPerson(clientId: String): PersonModel?

    suspend fun addClient(
        associatedPersonId: String,
        accountNumber: String,
        tariffId: String,
        address: AddressModel
    ): ClientModel?

    suspend fun changeTariff(
        clientId: String,
        newTariffId: String?
    ): Boolean

    suspend fun blockClientAccount(
        clientId: String
    ): Boolean

    suspend fun unblockClientAccount(
        clientId: String
    ): Boolean

    suspend fun addPositiveTransaction(
        clientId: String,
        amount: Float,
        note: TransactionNoteTextCode? = null
    )

    suspend fun addSoftNegativeTransaction(
        clientId: String,
        amount: Float,
        note: TransactionNoteTextCode? = null
    )

    suspend fun addStrictNegativeTransaction(
        clientId: String,
        amount: Float,
        note: TransactionNoteTextCode? = null
    )

    suspend fun connectService(
        clientId: String,
        serviceId: String
    ): Boolean

    suspend fun disconnectService(
        clientId: String,
        serviceId: String
    ): Boolean

    // FOR MONTHLY PAYMENTS

    suspend fun getClientsForBillingDate(
        currentDateTimestamp: Long,
        minLockDateTimestamp: Long,
    ): List<ClientModel>

    suspend fun closeBillingMonth(
        clientId: String,
        nextBillingDate: Long,
        paymentAmount: Int
    )

    suspend fun connectPendingTariff(
        clientId: String
    ): Boolean

}