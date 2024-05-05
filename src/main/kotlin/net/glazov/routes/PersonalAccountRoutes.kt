package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.ServicesDataSource
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.model.response.SimpleResponse

private const val PATH = "/api/account"

fun Route.personalAccountRoutes(
    persons: PersonsDataSource,
    clients: ClientsDataSource,
    employees: EmployeesDataSource,
    services: ServicesDataSource
) {

    authenticate {

        put("$PATH/update-password") {
            val personId = call.request.headers["person_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val oldPassword = call.request.headers["old_password"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val newPassword = call.request.headers["new_password"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val result = persons.changeAccountPassword(
                personId = personId,
                oldPassword = oldPassword,
                newPassword = newPassword
            )
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "password updated" else "unable to update password",
                    data = Unit
                )
            )
        }

        get("$PATH/person-info") {
            val personId = call.request.headers["person_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val personInfo = persons.getPersonById(personId) ?: kotlin.run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(
                SimpleResponse(
                    data = personInfo,
                    message = "person retrieved",
                    status = true
                )
            )
        }

    }

    authenticate("client") {

        get("$PATH/client-info") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val clientInfo = clients.getClientById(clientId) ?: kotlin.run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "client info received",
                    data = clientInfo
                )
            )
        }

        put("$PATH/update-tariff") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val tariffId = call.request.headers["tariff_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
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

        put("$PATH/block") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val result = clients.setIsAccountActive(
                clientId = clientId,
                newStatus = false
            )
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "account blocked" else "unable to block account",
                    data = Unit
                )
            )

        }

        put("$PATH/add-funds") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val amount = call.request.headers["amount"]?.toFloatOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val note = call.request.headers["note"]
            try {
                clients.addPositiveTransaction(
                    clientId = clientId,
                    amount = amount,
                    note = note
                )
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = "transaction complete",
                        data = Unit
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = e.message.toString(),
                        data = Unit
                    )
                )
            }
        }

        get("$PATH/services") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val allServices = services.getAllServices()
            val clientInfo = clients.getClientById(clientId) ?: kotlin.run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            val connectedServices = allServices.filter { clientInfo.connectedServices.contains(it.id) }
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "services retrieved",
                    data = connectedServices
                )
            )
        }

        put("$PATH/connect-service") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val serviceId = call.request.headers["service_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val service = services.getServiceById(serviceId) ?: kotlin.run {
                call.respond(HttpStatusCode.NotFound)
                return@put
            }
            if (service.isActive) {
                val result = clients.connectService(clientId, serviceId)
                call.respond(
                    SimpleResponse(
                        status = result,
                        message = if (result) "service connected" else "unable to connect this service",
                        data = Unit
                    )
                )
            } else call.respond(HttpStatusCode.BadRequest)
        }

        put("$PATH/disconnect-service") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val serviceId = call.request.headers["service_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            services.getServiceById(serviceId) ?: kotlin.run {
                call.respond(HttpStatusCode.NotFound)
                return@put
            } // checking if this service really exists
            val result = clients.connectService(clientId, serviceId)
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "service disconnected" else "unable to disconnect this service",
                    data = Unit
                )
            )
        }

    }

    authenticate("employee") {

        get("$PATH/employee-info") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val employee = employees.getEmployeeById(employeeId) ?: kotlin.run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "employee retrieved",
                    data = employee
                )
            )
        }

    }

}