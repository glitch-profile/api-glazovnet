package net.glazov.data.utils.notificationsmanager

sealed class NotificationChannel(val channel: String) {
    data object News: NotificationChannel("news")
    data object Tariffs: NotificationChannel("tariffs")
    data object Announcements: NotificationChannel("announcements")
    data object Chat: NotificationChannel("chat")
    data object AccountWarnings: NotificationChannel("account_warnings")
    data object ServiceNews: NotificationChannel("service_posts")
    data object Other: NotificationChannel("other")
}