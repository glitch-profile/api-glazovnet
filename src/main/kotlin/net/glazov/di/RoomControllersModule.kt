package net.glazov.di

import net.glazov.rooms.RequestsRoomController
import org.koin.dsl.module

val roomControllersModule = module {

    single<RequestsRoomController> {
        RequestsRoomController()
    }

}