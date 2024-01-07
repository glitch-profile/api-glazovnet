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
}