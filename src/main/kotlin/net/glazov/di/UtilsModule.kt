package net.glazov.di

import io.ktor.server.config.*
import net.glazov.data.utils.filemanager.FileManager
import net.glazov.data.utils.filemanager.FileManagerImpl
import net.glazov.data.utils.notificationsmanager.NotificationManagerImpl
import net.glazov.data.utils.notificationsmanager.NotificationsManager
import net.glazov.data.utils.paymentmanager.TransactionManager
import net.glazov.data.utils.paymentmanager.TransactionManagerImpl
import org.koin.dsl.module

private val BASE_URL = ApplicationConfig(null).tryGetString("storage.base_url").toString()

val utilsModule = module {

    single<FileManager> {
        FileManagerImpl(
            baseUrl = BASE_URL
        )
    }
    single<TransactionManager> {
        TransactionManagerImpl()
    }
    single<NotificationsManager> {
        NotificationManagerImpl()
    }

}