package net.glazov.data.utils.notificationsmanager

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import net.glazov.data.datasource.users.PersonsDataSource

class NotificationManagerImpl(
    private val persons: PersonsDataSource
): NotificationsManager {

    private fun generateAndroidDataConfig(
        translatableData: TranslatableNotificationData,
        imageUrl: String?,
        notificationChannel: NotificationChannel,
        deepLink: String?
    ): AndroidConfig {
        return AndroidConfig.builder()
            .apply {
                if (translatableData.title != null) {
                    putData("title", translatableData.title)
                } else if (translatableData.titleResource != null) {
                    putData("title_loc_key", translatableData.titleResource)
                    if (!translatableData.titleArgs.isNullOrEmpty()) {
                        if (translatableData.titleArgs.size == 1) {
                            putData("title_loc_args", translatableData.titleArgs.single())
                        } else putData("title_loc_args", translatableData.titleArgs.joinToString(", "))
                    }
                } else throw NotificationBuildError("title is empty")
            }
            .apply {
                if (translatableData.body != null) {
                    putData("body", translatableData.body)
                } else if (translatableData.bodyResource != null) {
                    putData("body_loc_key", translatableData.bodyResource)
                    if (!translatableData.bodyArgs.isNullOrEmpty()) {
                        if (translatableData.bodyArgs.size == 1) {
                            putData("body_loc_args", translatableData.bodyArgs.single())
                        } else putData("body_loc_args", translatableData.bodyArgs.joinToString(", "))
                    }
                } else throw NotificationBuildError("body is empty")
            }
            .apply {
                if (imageUrl != null) {
                    putData("image", imageUrl)
                }
            }
            .apply {
                if (deepLink != null) {
                    putData("deeplink", deepLink)
                }
            }
            .putData("channel_id", notificationChannel.channel)
            .build()
    }

    override suspend fun sendTranslatableNotificationByTopic(
        topic: NotificationsTopicsCodes,
        translatableData: TranslatableNotificationData,
        imageUrl: String?,
        notificationChannel: NotificationChannel,
        deepLink: Deeplink?
    ) {
        val tokens = persons.getPersonTokensWithSelectedTopic(topic)
        if (tokens.isNotEmpty()) {
            val androidConfig = generateAndroidDataConfig(
                translatableData = translatableData,
                imageUrl = imageUrl,
                notificationChannel = notificationChannel,
                deepLink = deepLink?.route
            )
            val messagesList = tokens.flatten().map { token ->
                Message.builder()
                    .setAndroidConfig(androidConfig)
                    .setToken(token)
                    .build()
            }
            if (messagesList.isNotEmpty()) FirebaseMessaging.getInstance().sendEach(messagesList)
        }
    }

    override suspend fun sendTranslatableNotificationByTokens(
        personsTokensList: List<List<String>>,
        translatableData: TranslatableNotificationData,
        imageUrl: String?,
        notificationChannel: NotificationChannel,
        deepLink: Deeplink?
    ) {
        if (personsTokensList.isNotEmpty()) {
            val androidConfig = generateAndroidDataConfig(
                translatableData = translatableData,
                imageUrl = imageUrl,
                notificationChannel = notificationChannel,
                deepLink = deepLink?.route
            )
            val messagesList = personsTokensList.flatten().map { token ->
                Message.builder()
                    .setAndroidConfig(androidConfig)
                    .setToken(token)
                    .build()
            }
            if (messagesList.isNotEmpty()) FirebaseMessaging.getInstance().sendEach(messagesList)
        }
    }

    override suspend fun sendTranslatableNotificationByPersonsId(
        personsId: List<String>,
        translatableData: TranslatableNotificationData,
        imageUrl: String?,
        notificationChannel: NotificationChannel,
        deepLink: Deeplink?
    ) {
        val personsTokens = personsId.mapNotNull { personId ->
            persons.getPersonById(personId)?.fcmTokensList
        }
        if (personsTokens.isNotEmpty()) {
            val androidConfig = generateAndroidDataConfig(
                translatableData = translatableData,
                imageUrl = imageUrl,
                notificationChannel = notificationChannel,
                deepLink = deepLink?.route
            )
            val messagesList = personsTokens.flatten().map { token ->
                Message.builder()
                    .setAndroidConfig(androidConfig)
                    .setToken(token)
                    .build()
            }
            if (messagesList.isNotEmpty()) FirebaseMessaging.getInstance().sendEach(messagesList)
        }
    }

}