package net.glazov.di

import io.ktor.server.config.*
import net.glazov.data.utils.FileManager
import net.glazov.data.utils.FileManagerImpl
import org.koin.dsl.module

private val BASE_URL = ApplicationConfig(null).tryGetString("storage.base_url").toString()

val utilsModule = module {

    single<FileManager> {
        FileManagerImpl(
            baseUrl = BASE_URL
        )
    }

}