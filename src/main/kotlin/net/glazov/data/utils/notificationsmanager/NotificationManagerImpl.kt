package net.glazov.data.utils.notificationsmanager

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import net.glazov.data.datasource.ClientsDataSource

class NotificationManagerImpl(
    private val clients: ClientsDataSource,
): NotificationsManager {

    private fun convertDataToNotification(
        title: String,
        body: String,
        imageUrl: String?
    ): Notification {
        return Notification.builder()
            .setTitle(title)
            .setBody(body)
            .apply {
                if (imageUrl != null) {
                    setImage(imageUrl)
                }
            }
            .build()
    }

    override suspend fun sendNotificationToClient(
        clientToken: String,
        title: String,
        body: String,
        imageUrl: String?
    ) {
        val message = Message.builder()
            .setNotification(
                convertDataToNotification(title, body, imageUrl)
            )
            .setToken(clientToken)
            .build()
        val response = FirebaseMessaging.getInstance().send(message)
        println("Notification send result: $response")
    }

    override suspend fun sendNotificationToMultipleClients(
        topic: NotificationsTopics,
        title: String,
        body: String,
        imageUrl: String?
    ) {
        val clientsTokens = clients.getClientsTokensWithSelectedTopic(topic)
        if (clientsTokens.isNotEmpty()) {
            val messages = clientsTokens.map { token ->
                Message.builder()
                    .setNotification(
                        convertDataToNotification(title, body, imageUrl)
                    )
                    .setToken(token)
                    .build()
            }
            FirebaseMessaging.getInstance().sendEach(messages)
        }
    }

    override suspend fun sendNotificationToMultipleClients(
        clientsTokens: List<String>,
        title: String,
        body: String,
        imageUrl: String?
    ) {
        if (clientsTokens.isNotEmpty()) {
            val messages = clientsTokens.map { token ->
                Message.builder()
                    .setNotification(
                        convertDataToNotification(title, body, imageUrl)
                    )
                    .setToken(token)
                    .build()
            }
            FirebaseMessaging.getInstance().sendEach(messages)
        }
    }

    override suspend fun sendNotificationToTopic(
        topic: NotificationsTopics,
        title: String,
        body: String,
        imageUrl: String?
    ) {
        val message = Message.builder()
            .setNotification(
                convertDataToNotification(title, body, imageUrl)
            )
            .setTopic(topic.name)
            .build()
        FirebaseMessaging.getInstance().send(message)
    }
}