package net.glazov.routes

import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.InnerDataSource
import net.glazov.data.datasource.TariffsDataSource
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.model.tariffs.TariffModel
import net.glazov.data.utils.EmployeeRoles
import net.glazov.data.utils.RequestTariffsAccess
import net.glazov.data.utils.notificationsmanager.*

private const val PATH = "/api/tariffs"
private val useInnerTariffs = ApplicationConfig(null).tryGetString("inner_data.use_protected_tariffs").toBoolean()

fun Route.tariffsRoutes(
    tariffs: TariffsDataSource,
    innerDataSource: InnerDataSource,
    notificationsManager: NotificationsManager,
    employees: EmployeesDataSource,
    clients: ClientsDataSource
) {

    authenticate {

        get(PATH) {
            try {
                val clientId = call.request.headers["client_id"]
                val employeeId = call.request.headers["employee_id"]
                var tariffsAccessLevel: RequestTariffsAccess = RequestTariffsAccess.Default
                if (employeeId != null) {
                    val employee = employees.getEmployeeById(employeeId)
                    if (employee != null) tariffsAccessLevel = RequestTariffsAccess.Employee
                } else if (clientId != null) {
                    val client = clients.getClientById(clientId)
                    val isClientAsOrganization = client?.connectedOrganizationName != null
                    if (isClientAsOrganization) tariffsAccessLevel = RequestTariffsAccess.Organization
                }
                val result = if (useInnerTariffs) innerDataSource.getAllInnerTariffs(tariffsAccessLevel)
                else tariffs.getAllTariffs(tariffsAccessLevel)
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = "all tariffs received",
                        data = result
                    )
                )
            } catch (e: ResponseException) {
                println("tariffs receive error - ${e.stackTrace}")
                call.respond(HttpStatusCode.InternalServerError)
            }

        }

        get("$PATH/active") {
            try {
                val clientId = call.request.headers["client_id"]
                val employeeId = call.request.headers["employee_id"]
                var tariffsAccessLevel: RequestTariffsAccess = RequestTariffsAccess.Default
                if (employeeId != null) {
                    val employee = employees.getEmployeeById(employeeId)
                    if (employee != null) tariffsAccessLevel = RequestTariffsAccess.Employee
                } else if (clientId != null) {
                    val client = clients.getClientById(clientId)
                    val isClientAsOrganization = client?.connectedOrganizationName != null
                    if (isClientAsOrganization) tariffsAccessLevel = RequestTariffsAccess.Organization
                }
                val result = if (useInnerTariffs) innerDataSource.getActiveInnerTariffs(tariffsAccessLevel)
                else tariffs.getActiveTariffs(tariffsAccessLevel)
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = "active tariffs received",
                        data = result
                    )
                )
            } catch (e: ResponseException) {
                println("tariffs receive error - ${e.stackTrace}")
                call.respond(HttpStatusCode.InternalServerError)
            }

        }

        get("$PATH/archive") {
            try {
                val clientId = call.request.headers["client_id"]
                val employeeId = call.request.headers["employee_id"]
                var tariffsAccessLevel: RequestTariffsAccess = RequestTariffsAccess.Default
                if (employeeId != null) {
                    val employee = employees.getEmployeeById(employeeId)
                    if (employee != null) tariffsAccessLevel = RequestTariffsAccess.Employee
                } else if (clientId != null) {
                    val client = clients.getClientById(clientId)
                    val isClientAsOrganization = client?.connectedOrganizationName != null
                    if (isClientAsOrganization) tariffsAccessLevel = RequestTariffsAccess.Organization
                }
                val result = if (useInnerTariffs) innerDataSource.getArchiveInnerTariffs(tariffsAccessLevel)
                else tariffs.getArchiveTariffs(tariffsAccessLevel)
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = "archive tariffs received",
                        data = result
                    )
                )
            } catch (e: ResponseException) {
                println("tariffs receive error - ${e.stackTrace}")
                call.respond(HttpStatusCode.InternalServerError)
            }

        }

        get("$PATH/{tariff_id}") {
            val tariffId = call.parameters["tariff_id"]
            if (tariffId !== null) {
                val result = tariffs.getTariffById(tariffId)
                call.respond(
                    SimpleResponse(
                        status = result !== null,
                        message = if (result !== null) "tariff found" else "tariff not found",
                        data = result
                    )
                )
            } else call.respond(HttpStatusCode.BadRequest)
        }
    }

    authenticate("client") {

        put("$PATH/update-tariff-for-client") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val tariffId = call.request.headers["tariff_id"]
            if (tariffId != null) {
                val isClientAsOrganization = clients.getClientById(clientId)?.connectedOrganizationName != null
                val tariff = tariffs.getTariffById(tariffId) ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }
                if (tariff.isActive) {
                    if (tariff.isForOrganization != isClientAsOrganization) {
                        call.respond(HttpStatusCode.Forbidden)
                        return@put
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                    return@put
                }
            }
            val result = clients.changeTariff(clientId = clientId, newTariffId = tariffId)
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "tariff updated" else "unable to update tariff",
                    data = Unit
                )
            )
        }

    }

    authenticate("employee") {

        post("$PATH/add") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.TARIFFS)) {
                call.respond(HttpStatusCode.Forbidden)
                return@post
            }
            val newTariff = call.receiveNullable<TariffModel>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val tariff = tariffs.addTariff(newTariff)
            val status = tariff != null
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "tariff added" else "error while adding the tariff",
                    data = tariff
                )
            )
            if (tariff !== null) {
                notificationsManager.sendTranslatableNotificationByTopic(
                    topic = NotificationsTopicsCodes.TARIFFS,
                    translatableData = TranslatableNotificationData.NewTariff(
                        tariffName = tariff.name
                    ),
                    notificationChannel = NotificationChannel.Tariffs,
                    deepLink = Deeplink.Tariff(tariff.id)
                )
            }
        }

        delete("$PATH/remove") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.TARIFFS)) {
                call.respond(HttpStatusCode.Forbidden)
                return@delete
            }
            val tariffId = call.request.queryParameters["tariff_id"]
            val status = tariffs.deleteTariff(tariffId = tariffId.toString())
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "tariff deleted" else "no tariff found",
                    data = Unit
                )
            )
        }

        put("$PATH/edit") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.TARIFFS)) {
                call.respond(HttpStatusCode.Forbidden)
                return@put
            }
            val newTariff = call.receiveNullable<TariffModel>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val status = tariffs.updateTariff(newTariff)
            call.respond(
                SimpleResponse(
                    status = status,
                    message = if (status) "tariff updated" else "error while updating the tariff",
                    data = Unit
                )
            )
        }
    }
}