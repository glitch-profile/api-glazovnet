package net.glazov.data.utils

import io.ktor.server.config.*

object UrlChanger {

    private const val CURRENT_URL = "http://82.179.120.68:8080"
    private val baseUrl = ApplicationConfig(null).tryGetString("storage.base_url").toString()

    fun toCurrentUrl(sourcePath: String): String {
        return sourcePath.replace(oldValue = baseUrl, newValue = CURRENT_URL)
    }

}