package net.glazov.data.datasource.users

import net.glazov.data.model.AddressModel
import net.glazov.data.model.users.ClientModel

interface ClientsDataSource {

    suspend fun getClientById(clientId: String): ClientModel?

    suspend fun addClient(
        associatedPersonId: String,
        accountNumber: String,
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
        amount: Double,
        note: String? = null
    )

    suspend fun addNegativeTransaction(
        clientId: String,
        amount: Double,
        note: String? = null
    )

}