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

    suspend fun login(
        login: String?,
        password: String?
    ): String?
}