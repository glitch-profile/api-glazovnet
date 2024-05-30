package net.glazov.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.*
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.utils.filemanager.FileManager
import net.glazov.data.utils.notificationsmanager.NotificationsManager
import net.glazov.rooms.RequestChatRoomController
import net.glazov.rooms.RequestsRoomController
import net.glazov.routes.*
import org.koin.ktor.ext.inject
import java.io.File
import java.nio.file.Paths

fun Application.configureRouting() {

    //posts
    val postsDataSource by inject<PostsDataSource>()
    //tariffs
    val tariffsDataSource by inject<TariffsDataSource>()
    //announcements for addresses
    val announcementsDataSource by inject<AnnouncementsDataSource>()
    val addressesDataSource by inject<AddressesDataSource>()
    //services
    val servicesDataSource by inject<ServicesDataSource>()
    //inner posts
    val innerPostsDataSource by inject<InnerPostsDataSource>()
    //clients
    val personsDataSource by inject<PersonsDataSource>()
    val clientsDataSource by inject<ClientsDataSource>()
    val employeesDataSource by inject<EmployeesDataSource>()
    val transactionsDataSource by inject<TransactionsDataSource>()
    val notificationManager by inject<NotificationsManager>()
    //support requests
    val requestsRoomController by inject<RequestsRoomController>()
    val requestChatRoomController by inject<RequestChatRoomController>()
    val chatDataSource by inject<ChatDataSource>()
    //utils
    val fileManager by inject<FileManager>()
    //RAW DATA
    val innerDataSource by inject<InnerDataSource>()

    routing {
        staticFiles(
            "/images",
            File("${Paths.get("").toAbsolutePath()}/static/images")) //http://url:8080/images/filename

        authRoutes(personsDataSource, clientsDataSource, employeesDataSource)
        postRoutes(posts = postsDataSource, notificationManager, employeesDataSource)
        tariffsRoutes(tariffsDataSource, innerDataSource, notificationManager, employeesDataSource, clientsDataSource)
        addressRoutes(addressesDataSource, employeesDataSource)
//        clientsRoutes(clientsDataSource)
        announcementsRoutes(announcementsDataSource, notificationManager, clientsDataSource, employeesDataSource)
        requestsRoute(requestsRoomController, requestChatRoomController, chatDataSource, employeesDataSource)
        utilRoutes(fileManager)
        notificationsRoutes(personsDataSource, clientsDataSource, employeesDataSource)
        usersRoutes(personsDataSource, clientsDataSource, employeesDataSource, transactionsDataSource)
        servicesRoutes(servicesDataSource, clientsDataSource)
        innerPostsRoutes(innerDataSource, innerPostsDataSource, notificationManager)
//        testRoutes()
//        innerRoutes(innerData = innerDataSource)
    }

}
