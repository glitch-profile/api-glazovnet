package net.glazov.data.utils.notificationsmanager

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import net.glazov.data.datasource.ClientsDataSource

class NotificationManagerImpl(
    private val clients: ClientsDataSource,
): NotificationsManager {

    private fun generateAndroidConfig(
        translatableData: TranslatableNotificationData,
        imageUrl: String?,
        priority: AndroidNotification.Priority
    ): AndroidConfig {
        return AndroidConfig.builder()
            .setNotification(
                AndroidNotification.builder()
                    .apply {
                        if (translatableData.title != null) {
                            setTitle(translatableData.title)
                        } else if (translatableData.titleResource != null) {
                            setTitleLocalizationKey(translatableData.titleResource)
                            if (!translatableData.titleArgs.isNullOrEmpty()) {
                                if (translatableData.titleArgs.size == 1) {
                                    addTitleLocalizationArg(translatableData.titleArgs.single())
                                } else addAllTitleLocalizationArgs(translatableData.titleArgs)
                            }
                        } else throw NotificationBuildError("title is empty")
                    }
                    .apply {
                        if (translatableData.body != null) {
                            setBody(translatableData.body)
                        } else if (translatableData.bodyResource != null) {
                            setBodyLocalizationKey(translatableData.bodyResource)
                            if (!translatableData.bodyArgs.isNullOrEmpty()) {
                                if (translatableData.bodyArgs.size == 1) {
                                    addBodyLocalizationArg(translatableData.bodyArgs.single())
                                } else addAllBodyLocalizationArgs(translatableData.bodyArgs)
                            }
                        } else throw NotificationBuildError("body is empty")
                    }
                    .apply {
                        if (imageUrl != null) {
                            setImage(imageUrl)
                        }
                    }
                    .setPriority(priority)
                    .build()
            ).build()
    }

    override suspend fun sendTranslatableNotificationToClientsByTopic(
        topic: NotificationsTopics,
        translatableData: TranslatableNotificationData,
        imageUrl: String?,
        priority: AndroidNotification.Priority
    ) {
        val clientsTokens = clients.getClientsTokensWithSelectedTopic(topic)
        if (clientsTokens.isNotEmpty()) {
            val androidConfig = generateAndroidConfig(
                translatableData = translatableData,
                imageUrl = imageUrl,
                priority = priority
            )
            val messagesList = clientsTokens.map { token ->
                Message.builder()
                    .setAndroidConfig(androidConfig)
                    .setToken(token)
                    .build()
            }
            FirebaseMessaging.getInstance().sendEach(messagesList)
        }
    }

    override suspend fun sendTranslatableNotificationToClientsByTokens(
        clientsTokens: List<String>,
        translatableData: TranslatableNotificationData,
        imageUrl: String?,
        priority: AndroidNotification.Priority
    ) {
        if (clientsTokens.isNotEmpty()) {
            val androidConfig = generateAndroidConfig(
                translatableData = translatableData,
                imageUrl = imageUrl,
                priority = priority
            )
            val messagesList = clientsTokens.map { token ->
                Message.builder()
                    .setAndroidConfig(androidConfig)
                    .setToken(token)
                    .build()
            }
            FirebaseMessaging.getInstance().sendEach(messagesList)
        }
    }

    override suspend fun sendTranslatableNotificationToClientsById(
        clientsId: List<String>,
        translatableData: TranslatableNotificationData,
        imageUrl: String?,
        priority: AndroidNotification.Priority
    ) {
        val clientsTokens = clientsId.mapNotNull { clientId ->
            clients.getClientById(clientId)?.fcmToken
        }
        if (clientsTokens.isNotEmpty()) {
            val androidConfig = generateAndroidConfig(
                translatableData = translatableData,
                imageUrl = imageUrl,
                priority = priority
            )
            val messagesList = clientsTokens.map { token ->
                Message.builder()
                    .setAndroidConfig(androidConfig)
                    .setToken(token)
                    .build()
            }
            FirebaseMessaging.getInstance().sendEach(messagesList)
        }
    }
}