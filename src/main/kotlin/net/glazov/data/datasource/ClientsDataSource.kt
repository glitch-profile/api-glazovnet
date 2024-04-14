package net.glazov.data.datasource

import net.glazov.data.model.ClientModel
import net.glazov.data.utils.notificationsmanager.NotificationsTopicsCodes

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

    suspend fun addFcmToken(
        userId: String,
        newToken: String
    ): Boolean

    suspend fun removeFcmToken(
        userId: String,
        tokenToRemove: String
    ): Boolean

    suspend fun updateNotificationTopics(
        clientId: String,
        newTopicsList: List<String>
    ): Boolean

    suspend fun updateNotificationsStatus(
        clientId: String,
        newStatus: Boolean
    ): Boolean

    suspend fun getClientsTokensWithSelectedTopic(
        topic: NotificationsTopicsCodes
    ): List<List<String>>

    suspend fun changeAccountPassword(
        userId: String,
        oldPassword: String,
        newPassword: String
    ): Boolean

    suspend fun changeTariff(
        userId: String,
        newTariffId: String
    ): Boolean

    suspend fun setIsAccountActive(
        userId: String,
        newStatus: Boolean
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