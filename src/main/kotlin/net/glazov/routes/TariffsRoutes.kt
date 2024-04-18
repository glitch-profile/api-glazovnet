package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.TariffsDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.model.TariffModel
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.model.response.SimpleTariffResponse
import net.glazov.data.utils.employeesroles.EmployeeRoles
import net.glazov.data.utils.notificationsmanager.*

private const val PATH = "/api/tariffs"

fun Route.tariffsRoutes(
    tariffs: TariffsDataSource,
    notificationsManager: NotificationsManager,
    employees: EmployeesDataSource
) {

    authenticate {
        get("$PATH/") {
            val tariffsList = tariffs.getAllTariffs()
            call.respond(
                SimpleTariffResponse(
                    status = true,
                    message = "${tariffsList.size} tariffs retrieved",
                    data = tariffsList
                )
            )
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
                SimpleTariffResponse(
                    status = status,
                    message = if (status) "tariff added" else "error while adding the tariff",
                    data = if (status) listOf(tariff!!) else emptyList()
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
                SimpleTariffResponse(
                    status = status,
                    message = if (status) "tariff deleted" else "no tariff found",
                    data = emptyList()
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
                SimpleTariffResponse(
                    status = status,
                    message = if (status) "tariff updated" else "error while updating the tariff",
                    data = emptyList()
                )
            )
        }
    }
}