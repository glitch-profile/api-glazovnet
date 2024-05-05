package net.glazov.data.utils.notificationsmanager

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

enum class NotificationsTopicsCodes {
    NEWS,
    TARIFFS,
    ANNOUNCEMENTS,
    PERSONAL_ACCOUNT_WARNINGS,
    SERVICE_NEWS
}

enum class NotificationTopicVisibility {
    DEFAULT,
    CLIENT,
    EMPLOYEE,
}

@Serializable
data class NotificationTopic(
    val topicCode: NotificationsTopicsCodes,
    @Transient
    val visibility: NotificationTopicVisibility = NotificationTopicVisibility.DEFAULT,
    val name: String,
    val nameEn: String,
    val description: String,
    val descriptionEn: String
) {

    companion object {
        fun  all(
            includeClientTopics: Boolean,
            includeEmployeeTopics: Boolean
        ): List<NotificationTopic> {
            val visibilityList = buildList<NotificationTopicVisibility> {
                add(NotificationTopicVisibility.DEFAULT)
                if (includeClientTopics) add(NotificationTopicVisibility.CLIENT)
                if (includeEmployeeTopics) add(NotificationTopicVisibility.EMPLOYEE)
            }

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
                visibility = NotificationTopicVisibility.CLIENT,
                name = "Объявления",
                nameEn = "Announcements",
                description = "Информация об объявлениях по Вашему адресу",
                descriptionEn = "Information about announcements at your address"
            )
            val serviceNews = NotificationTopic(
                topicCode = NotificationsTopicsCodes.SERVICE_NEWS,
                visibility = NotificationTopicVisibility.EMPLOYEE,
                name = "Служебные новости",
                nameEn = "Service news",
                description = "Быстрый доступ ко всем служебным новостям",
                descriptionEn = "Quick access to all service news"
            )
            val accountWarnings =  NotificationTopic(
                topicCode = NotificationsTopicsCodes.PERSONAL_ACCOUNT_WARNINGS,
                visibility = NotificationTopicVisibility.CLIENT,
                name = "Учетная запись",
                nameEn = "Personal account",
                description = "Предупреждения о событиях в Личном кабинете",
                descriptionEn = "Alerts on events in your Personal account"
            )

            val filteredList = listOf(news, tariffs, announcements, serviceNews, accountWarnings).filter { topic ->
                visibilityList.contains(topic.visibility)
            }
            return filteredList
        }
    }

}





