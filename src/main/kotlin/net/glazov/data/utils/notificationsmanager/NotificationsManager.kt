package net.glazov.data.utils.notificationsmanager

interface NotificationsManager {

    suspend fun sendTranslatableNotificationToClientsByTopic(
        topic: NotificationsTopicsCodes,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        notificationChannel: NotificationChannel,
        deepLink: String? = null
    )

    suspend fun sendTranslatableNotificationToClientsByTokens(
        clientsTokensLists: List<List<String>>,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        notificationChannel: NotificationChannel,
        deepLink: String? = null
    )

    suspend fun sendTranslatableNotificationToClientsById(
        clientsId: List<String>,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        notificationChannel: NotificationChannel = NotificationChannel.Other,
        deepLink: String? = null
    )

}