package net.glazov.data.utils.notificationsmanager

interface NotificationsManager {

    suspend fun sendTranslatableNotificationByTopic(
        topic: NotificationsTopicsCodes,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        notificationChannel: NotificationChannel,
        deepLink: Deeplink? = null
    )

    suspend fun sendTranslatableNotificationByTokens(
        personsTokensList: List<List<String>>,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        notificationChannel: NotificationChannel,
        deepLink: Deeplink? = null
    )

    suspend fun sendTranslatableNotificationByPersonsId(
        personsId: List<String>,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        notificationChannel: NotificationChannel = NotificationChannel.Other,
        deepLink: Deeplink? = null
    )

}