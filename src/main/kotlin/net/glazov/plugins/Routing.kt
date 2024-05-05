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
    val innerPostsDataSource by inject<InnerPostsDataSource>()
    val servicesDataSource by inject<ServicesDataSource>()
    //RAW DATA
    val innerDataSource by inject<InnerDataSource>()

    routing {
        staticFiles(
            "/images",
            File("${Paths.get("").toAbsolutePath()}/static/images")) //http://url:8080/images/filename

        authRoutes(personsDataSource, clientsDataSource, employeesDataSource)
        postRoutes(posts = postsDataSource, notificationManager, employeesDataSource)
        tariffsRoutes(tariffsDataSource, innerDataSource, notificationManager, employeesDataSource)
        addressRoutes(addressesDataSource, employeesDataSource)
//        clientsRoutes(clientsDataSource)
        announcementsRoutes(announcementsDataSource, notificationManager, clientsDataSource, employeesDataSource)
        requestsRoute(requestsRoomController, requestChatRoomController, chatDataSource, employeesDataSource)
        utilRoutes(fileManager)
        notificationsRoutes(personsDataSource, clientsDataSource, employeesDataSource)
        personalAccountRoutes(personsDataSource, clientsDataSource, employeesDataSource)
        servicesRoutes(servicesDataSource, clientsDataSource)
        innerPostsRoutes(innerDataSource, innerPostsDataSource)
//        testRoutes()
//        innerRoutes(innerData = innerDataSource)
    }

//    val scope = CoroutineScope(Dispatchers.Default)
//    scope.launch {
//        servicesDataSource.addService(
//            name = "Обещанный платеж",
//            description = "Возможность пользоваться услугами без оплаты в течение 48 часов.\nПо окончании расчётного периода списывается полная стоимость тарифного плана, даже при невнесении денежных средств",
//            nameEn = "Promised payment",
//            descriptionEn = "The possibility to use services without payment for 48 hours.\nAt the end of the billing period, the full price of the tariff plan is deducted, even in case of non-payment of funds",
//            costPerMonth = 0.0f
//        )
//        servicesDataSource.addService(
//            name = "IPTV - Начальный",
//            description = "Доступ к просмотру 115 каналов на пяти устройствах",
//            nameEn = "IPTV - Initial",
//            descriptionEn = "Access to watch 115 channels on up to five devices",
//            costPerMonth = 0.0f
//        )
//        servicesDataSource.addService(
//            name = "IPTV - Популярный",
//            description = "Доступ к просмотру 139 каналов на пяти устройствах",
//            nameEn = "IPTV - Popular",
//            descriptionEn = "Access to watch 139 channels on up to five devices",
//            costPerMonth = 250.0f
//        )
//        servicesDataSource.addService(
//            name = "IPTV - Премиум",
//            description = "Доступ к просмотру 146 каналов на пяти устрйоствах",
//            nameEn = "IPTV - Premium",
//            descriptionEn = "Access to watch 146 channels on up to five devices",
//            costPerMonth = 350.0f
//        )
//        servicesDataSource.addService(
//            name = "Статический IP адрес",
//            description = "Использование постоянного IP адреса для доступа к собственным Интернет ресурсам",
//            nameEn = "Static IP address",
//            descriptionEn = "Using a permanent IP address to access your own Internet resources",
//            costPerMonth = 30.0f
//        )
//        servicesDataSource.addService(
//            name = "Уведомления по СМС",
//            description = "Использование уведомлений по СМС в случае, когда не работают PUSH-уведомления или нет постоянного доступа в Интернет",
//            nameEn = "SMS notifications",
//            descriptionEn = "Using SMS notifications when PUSH notifications do not work or there is no permanent Internet access",
//            costPerMonth = 0.0f
//        )
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
