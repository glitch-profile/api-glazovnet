package net.glazov.data.utils

import io.ktor.server.config.*

object UrlChanger {

    private val enabled =
        ApplicationConfig(null).tryGetString("misc.is_url_changer_active")?.toBooleanStrict() ?: true

    fun toCurrentUrl(sourcePath: String): String {
        return if (enabled) {
            val baseUrl = ApplicationConfig(null).tryGetString("storage.base_url").toString()
            val newUrl = ApplicationConfig(null).tryGetString("misc.temporal_url").toString()
            sourcePath.replace(oldValue = baseUrl, newValue = newUrl)
        } else sourcePath
    }

}