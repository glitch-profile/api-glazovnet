package net.glazov.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.*
import net.glazov.data.utils.FileManager
import net.glazov.rooms.RequestChatRoomController
import net.glazov.rooms.RequestsRoomController
import net.glazov.routes.*
import org.koin.ktor.ext.inject
import java.io.File
import java.nio.file.Paths

fun Application.configureRouting() {

    val postsDataSource by inject<PostsDataSource>()
    val tariffsDataSource by inject<TariffsDataSource>()
    val addressesDataSource by inject<AddressesDataSource>()
    val announcementsDataSource by inject<AnnouncementsDataSource>()
    val clientsDataSource by inject<ClientsDataSource>()
    val adminsDataSource by inject<AdminsDataSource>()
    val requestsRoomController by inject<RequestsRoomController>()
    val requestChatRoomController by inject<RequestChatRoomController>()
    val chatDataSource by inject<ChatDataSource>()
    val fileManager by inject<FileManager>()

    routing {
        staticFiles(
            "/images",
            File("${Paths.get("").toAbsolutePath()}/static/images")) //http://url:8080/images/filename

        authRoutes(clientsDataSource, adminsDataSource)
        postRoutes(postsDataSource)
        tariffsRoutes(tariffsDataSource)
        addressRoutes(addressesDataSource)
        clientsRoutes(clientsDataSource)
        announcementsRoutes(announcementsDataSource)
        requestsRoute(requestsRoomController, requestChatRoomController, chatDataSource)
        utilRoutes(fileManager)
        //testRoutes()
    }
}
