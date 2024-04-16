package net.glazov.sessions

data class OAuthSession(
    val state: String,
    val token: String
)
