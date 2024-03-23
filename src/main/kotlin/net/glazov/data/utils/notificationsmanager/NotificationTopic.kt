package net.glazov.data.utils.notificationsmanager

sealed class NotificationTopic(
    val topicCode: NotificationsTopicsCodes,
    val name: String,
    val nameEn: String? = null,
    val description: String,
    val descriptionEn: String? = null
) {
    data object News: NotificationTopic(
        topicCode = NotificationsTopicsCodes.NEWS,
        name = "Новости",
        nameEn = "News",
        description = "Получайте информацию о всех новостях",
        descriptionEn = "NGet updates on all the news"
    )
    data object Tariffs: NotificationTopic(
        topicCode = NotificationsTopicsCodes.TARIFFS,
        name = "Тарифы",
        nameEn = "Tariffs",
        description = "Узнавайте первым о новых тарифах",
        descriptionEn = "Be the first to know about new tariffs"
    )
    data object Announcements: NotificationTopic(
        topicCode = NotificationsTopicsCodes.ANNOUNCEMENTS,
        name = "Объявления",
        nameEn = "Announcements",
        description = "Информация об объявлениях по Вашему адресу",
        descriptionEn = "Information about announcements at your address"
    )
    data object AccountWarnings: NotificationTopic(
        topicCode = NotificationsTopicsCodes.PERSONAL_ACCOUNT_WARNINGS,
        name = "Учетная запись",
        nameEn = "Personal account",
        description = "Предупреждения о событиях в Личном кабинете",
        descriptionEn = "Alerts on events in your Personal account"
    )

    companion object {

        fun  all(): List<NotificationTopic> {
            return listOf(News, Tariffs, Announcements, AccountWarnings)
        }

    }
}




