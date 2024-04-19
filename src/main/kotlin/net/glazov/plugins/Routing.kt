package net.glazov.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.glazov.data.datasource.*
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.model.AddressModel
import net.glazov.data.utils.employeesroles.EmployeeRoles
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
    val personsDataSource by inject<PersonsDataSource>()
    val clientsDataSource by inject<ClientsDataSource>()
    val employeesDataSource by inject<EmployeesDataSource>()
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

        authRoutes(personsDataSource, clientsDataSource, employeesDataSource)
        postRoutes(posts = postsDataSource, notificationManager, employeesDataSource)
        tariffsRoutes(tariffsDataSource, notificationManager, employeesDataSource)
        addressRoutes(addressesDataSource, employeesDataSource)
        clientsRoutes(clientsDataSource)
        announcementsRoutes(announcementsDataSource, notificationManager, clientsDataSource, employeesDataSource)
        requestsRoute(requestsRoomController, requestChatRoomController, chatDataSource, employeesDataSource)
        utilRoutes(fileManager)
        notificationsRoutes(personsDataSource)
        personalAccountRoutes(personsDataSource, clientsDataSource)
        //testRoutes()
        innerRoutes(innerData = innerDataSource)
    }

//    val scope = CoroutineScope(Dispatchers.Default)
//    scope.launch {
//        val person = personsDataSource.addPerson(
//            firstName = "Тест2",
//            lastName = "Тест2",
//            middleName = "Тест2",
//            login = "456",
//            password = "456"
//        )
//        val client = clientsDataSource.addClient(
//            associatedPersonId = person!!.id,
//            accountNumber = "0005",
//            address = AddressModel()
//        )
//        val employee = employeesDataSource.addEmployee(
//            associatedPersonId = person!!.id,
//            roles = listOf(EmployeeRoles.NEWS, EmployeeRoles.ANNOUNCEMENTS, EmployeeRoles.SUPPORT_CHAT)
//        )
//    }

}
