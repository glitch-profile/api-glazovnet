package net.glazov.data.datasource

import net.glazov.data.model.ClientModel

interface ClientsDataSource {

    suspend fun getAllClients(): List<ClientModel>

    suspend fun createClient(
        clientModel: ClientModel
    ): ClientModel?

    suspend fun getClientById(
        clientId: String
    ): ClientModel?

    suspend fun getClientNameById(
        clientId: String,
        useShortForm: Boolean = false
    ): String

    suspend fun login(
        login: String?,
        password: String?
    ): ClientModel?

    suspend fun updateFcmToken(
        userId: String,
        newToken: String?
    ): Boolean

    suspend fun changeAccountPassword(
        userId: String,
        oldPassword: String,
        newPassword: String
    ): Boolean

    suspend fun changeTariff(
        userId: String,
        newTariffId: String
    ): Boolean

    suspend fun addPositiveTransaction(
        userId: String,
        amount: Double,
        note: String? = null
    )

    suspend fun addNegativeTransaction(
        userId: String,
        amount: Double,
        note: String? = null
    )
}