package net.glazov.data.datasource.users

import net.glazov.data.model.AddressModel
import net.glazov.data.model.users.ClientModel
import net.glazov.data.model.users.PersonModel

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
        newTariffId: String
    ): Boolean

    suspend fun setIsAccountActive(
        clientId: String,
        newStatus: Boolean
    ): Boolean

    suspend fun addPositiveTransaction(
        clientId: String,
        amount: Float,
        note: String? = null
    )

    suspend fun addNegativeTransaction(
        clientId: String,
        amount: Float,
        note: String? = null
    )

}