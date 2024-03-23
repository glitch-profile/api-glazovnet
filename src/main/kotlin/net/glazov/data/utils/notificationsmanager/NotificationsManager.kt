package net.glazov.data.utils.notificationsmanager

interface NotificationsManager {

    suspend fun sendTranslatableNotificationToClientsByTopic(
        topic: NotificationsTopicsCodes,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        notificationChannel: NotificationChannel
    )

    suspend fun sendTranslatableNotificationToClientsByTokens(
        clientsTokensLists: List<List<String>>,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        notificationChannel: NotificationChannel
    )

    suspend fun sendTranslatableNotificationToClientsById(
        clientsId: List<String>,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        notificationChannel: NotificationChannel = NotificationChannel.Other
    )

}