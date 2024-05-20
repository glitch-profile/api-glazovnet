package net.glazov.data.utils.notificationsmanager

private const val BASE_ROUTE = "https://glazov.net"

sealed class Deeplink(
    val route: String
) {
    data object PostsList: Deeplink("$BASE_ROUTE/posts-list")
    data class Post(val postId: String) : Deeplink("$BASE_ROUTE/posts/$postId")
    data object TariffsList: Deeplink("$BASE_ROUTE/tariffs-list")
    data class Tariff(val tariffId: String) : Deeplink("$BASE_ROUTE/tariffs/$tariffId")
    data object AnnouncementsList: Deeplink("$BASE_ROUTE/announcements-list")
    data class Announcement(val announcementId: String): Deeplink("$BASE_ROUTE/announcements/$announcementId")
    data object SupportRequestsList: Deeplink("$BASE_ROUTE/requests-list")
    data class SupportRequest(val requestId: String) : Deeplink("$BASE_ROUTE/requests/$requestId")
    data class SupportChat(val requestId: String) : Deeplink("$BASE_ROUTE/requests/$requestId/chat")
    data object PersonalAccount: Deeplink("$BASE_ROUTE/personal-account")
    data object ServicePosts: Deeplink("$BASE_ROUTE/service-posts")
}