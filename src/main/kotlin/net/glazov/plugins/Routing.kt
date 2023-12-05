package net.glazov.plugins

import io.ktor.server.application.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.*
import net.glazov.routes.*
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    val apiKey = environment.config.property("storage.api_key").getString()

    val postsDataSource by inject<PostsDataSource>()
    val tariffsDataSource by inject<TariffsDataSource>()
    val addressesDataSource by inject<AddressesDataSource>()
    val announcementsDataSource by inject<AnnouncementsDataSource>()
    val clientsDataSource by inject<ClientsDataSource>()
    val chatDataSource by inject<ChatDataSource>()

    routing {
        //testRoutes()
        postRoutes(apiKey, postsDataSource)
        tariffsRoutes(apiKey, tariffsDataSource)
        addressRoutes(apiKey, addressesDataSource)
        clientsRoutes(apiKey, clientsDataSource)
        announcementsRoutes(apiKey, announcementsDataSource)
    }
}
