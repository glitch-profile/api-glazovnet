package net.glazov.di

import net.glazov.rooms.RequestChatRoomController
import net.glazov.rooms.RequestsRoomController
import org.koin.dsl.module

val roomControllersModule = module {

    single<RequestsRoomController> {
        RequestsRoomController()
    }

    single<RequestChatRoomController> {
        RequestChatRoomController(
            chat = get(),
            persons = get(),
            notificationsManager = get()
        )
    }
}