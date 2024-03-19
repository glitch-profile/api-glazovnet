package net.glazov.data.utils.notificationsmanager

import com.google.firebase.messaging.AndroidNotification

interface NotificationsManager {

    suspend fun sendTranslatableNotificationToClientsByTopic(
        topic: NotificationsTopics,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        priority: AndroidNotification.Priority = AndroidNotification.Priority.DEFAULT
    )

    suspend fun sendTranslatableNotificationToClientsByTokens(
        clientsTokens: List<String>,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        priority: AndroidNotification.Priority = AndroidNotification.Priority.DEFAULT
    )

    suspend fun sendTranslatableNotificationToClientsById(
        clientsId: List<String>,
        translatableData: TranslatableNotificationData,
        imageUrl: String? = null,
        priority: AndroidNotification.Priority = AndroidNotification.Priority.DEFAULT
    )

}