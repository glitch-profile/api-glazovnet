package net.glazov.data.utils.notificationsmanager

import kotlinx.serialization.Serializable

enum class NotificationsTopicsCodes {
    NEWS,
    TARIFFS,
    ANNOUNCEMENTS,
    PERSONAL_ACCOUNT_WARNINGS
}

@Serializable
data class NotificationTopic(
    val topicCode: NotificationsTopicsCodes,
    val name: String,
    val nameEn: String? = null,
    val description: String,
    val descriptionEn: String? = null
) {

    companion object {
        fun  all(): List<NotificationTopic> {
            val news = NotificationTopic(
                topicCode = NotificationsTopicsCodes.NEWS,
                name = "Новости",
                nameEn = "News",
                description = "Получайте информацию о всех новостях",
                descriptionEn = "Get updates on all the news"
            )
            val tariffs =  NotificationTopic(
                topicCode = NotificationsTopicsCodes.TARIFFS,
                name = "Тарифы",
                nameEn = "Tariffs",
                description = "Узнавайте первым о новых тарифах",
                descriptionEn = "Be the first to know about new tariffs"
            )
            val announcements = NotificationTopic(
                topicCode = NotificationsTopicsCodes.ANNOUNCEMENTS,
                name = "Объявления",
                nameEn = "Announcements",
                description = "Информация об объявлениях по Вашему адресу",
                descriptionEn = "Information about announcements at your address"
            )
            val accountWarnings =  NotificationTopic(
                topicCode = NotificationsTopicsCodes.PERSONAL_ACCOUNT_WARNINGS,
                name = "Учетная запись",
                nameEn = "Personal account",
                description = "Предупреждения о событиях в Личном кабинете",
                descriptionEn = "Alerts on events in your Personal account"
            )

            return listOf(news, tariffs, announcements, accountWarnings)
        }
    }

}





