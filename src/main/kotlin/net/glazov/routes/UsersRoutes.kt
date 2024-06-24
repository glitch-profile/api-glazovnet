package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.TransactionsDataSource
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.paymentmanager.TransactionNoteTextCode

private const val PERSONS_PATH = "/api/persons"
private const val CLIENTS_PATH = "/api/clients"
private const val EMPLOYEES_PATH = "/api/employees"

fun Route.usersRoutes(
    persons: PersonsDataSource,
    clients: ClientsDataSource,
    employees: EmployeesDataSource,
    transactions: TransactionsDataSource
) {

    authenticate {

        put("$PERSONS_PATH/update-password") {
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

        get("$PERSONS_PATH/info") {
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

        get("$CLIENTS_PATH/info") {
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

        put("$CLIENTS_PATH/block") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val result = clients.blockClientAccount(
                clientId = clientId
            )
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "account blocked" else "unable to block account",
                    data = Unit
                )
            )

        }

        put("$CLIENTS_PATH/add-funds") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            val amount = call.request.headers["amount"]?.toFloatOrNull() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
            if (amount < 0) {
                call.respond(
                    SimpleResponse(
                        status = false,
                        message = "incorrect amount",
                        data = Unit
                    )
                )
            }
            val note = call.request.headers["note"]
            try {
                clients.addPositiveTransaction(
                    clientId = clientId,
                    amount = amount,
                    note = if (note != null) TransactionNoteTextCode.CustomNote(note) else null
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

        get("$CLIENTS_PATH/balance-history") {
            val clientId = call.request.headers["client_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val startTimestamp = call.request.headers["start_timestamp"]?.toIntOrNull()
            val endTimestamp = call.request.headers["end_timestamp"]?.toIntOrNull()
            val transactionsList = if (startTimestamp == null && endTimestamp == null)
                transactions.getTransactionsForClient(clientId)
            else transactions.getTransactionsForClient(clientId, startTimestamp, endTimestamp)
            call.respond(
                SimpleResponse(
                    data = transactionsList,
                    message = "transactions retrieved",
                    status = true
                )
            )
        }

    }

    authenticate("employee") {

        get("$EMPLOYEES_PATH/info") {
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