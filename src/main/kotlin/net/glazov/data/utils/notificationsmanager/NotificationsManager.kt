package net.glazov.data.utils.notificationsmanager

interface NotificationsManager {

    suspend fun sendNotificationToClient(
        clientToken: String,
        title: String,
        body: String,
        imageUrl: String? = null
    )

    suspend fun sendNotificationToMultipleClients(
        clientsTokens: List<String>,
        title: String,
        body: String,
        imageUrl: String? = null
    )

    suspend fun sendNotificationToTopic(
        topic: NotificationsTopics,
        title: String,
        body: String,
        imageUrl: String? = null
    )
}