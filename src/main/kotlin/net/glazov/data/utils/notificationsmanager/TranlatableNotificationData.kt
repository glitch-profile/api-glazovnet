package net.glazov.data.utils.notificationsmanager

sealed class TranslatableNotificationData(
    val title: String? = null,
    val titleResource: String? = null,
    val titleArgs: List<String>? = null,
    val body: String? = null,
    val bodyResource: String? = null,
    val bodyArgs: List<String>? = null
) {
    data class NewPost(val postTitle: String, val postBody: String): TranslatableNotificationData(
        titleResource = "notifications_new_post_title",
        titleArgs = listOf(postTitle),
        body = postBody
    )
    data class NewServicePost(val postTitle: String?, val postBody: String): TranslatableNotificationData(
        titleResource = if (postTitle != null) "notifications_new_service_post_title_with_arg"
            else "notifications_new_service_post_title",
        titleArgs = if (postTitle != null) listOf(postTitle) else null,
        body = postBody
    )
    data class NewTariff(val tariffName: String): TranslatableNotificationData(
        titleResource = "notifications_new_tariff_title",
        titleArgs = listOf(tariffName),
        bodyResource = "notifications_new_tariff_body"
    )
    data class NewAnnouncements(val announcementTitle: String): TranslatableNotificationData(
        titleResource = "notifications_new_announcement_title",
        bodyResource = "notifications_new_announcement_body",
        body = announcementTitle
    )
    data class NewChatMessage(val requestTitle: String, val messageText: String): TranslatableNotificationData(
        titleResource = "notifications_new_message_title",
        titleArgs = listOf(requestTitle),
        body = messageText
    )

    data class Custom(
        val title: String? = null,
        val titleResource: String? = null,
        val titleArgs: List<String>? = null,
        val body: String? = null,
        val bodyResource: String? = null,
        val bodyArgs: List<String>? = null
    )
}