package net.glazov.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.*
import net.glazov.data.datasource.users.AdminsDataSourceOld
import net.glazov.data.datasource.users.ClientsDataSourceOld
import net.glazov.data.utils.filemanager.FileManager
import net.glazov.data.utils.notificationsmanager.NotificationsManager
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
    val clientsDataSourceOld by inject<ClientsDataSourceOld>()
    val adminsDataSourceOld by inject<AdminsDataSourceOld>()
    val requestsRoomController by inject<RequestsRoomController>()
    val requestChatRoomController by inject<RequestChatRoomController>()
    val chatDataSource by inject<ChatDataSource>()
    val fileManager by inject<FileManager>()
    val notificationManager by inject<NotificationsManager>()
    //RAW DATA
    val innerDataSource by inject<InnerDataSource>()

    routing {
        staticFiles(
            "/images",
            File("${Paths.get("").toAbsolutePath()}/static/images")) //http://url:8080/images/filename

        authRoutes(clientsDataSourceOld, adminsDataSourceOld)
        postRoutes(posts = postsDataSource, notificationManager)
        tariffsRoutes(tariffsDataSource, notificationManager)
        addressRoutes(addressesDataSource)
        clientsRoutes(clientsDataSourceOld)
        announcementsRoutes(announcementsDataSource, notificationManager)
        requestsRoute(requestsRoomController, requestChatRoomController, chatDataSource)
        utilRoutes(fileManager)
        notificationsRoutes(clientsDataSourceOld)
        personalAccountRoutes(clientsDataSourceOld)
        //testRoutes()
        innerRoutes(innerData = innerDataSource)
    }

}
