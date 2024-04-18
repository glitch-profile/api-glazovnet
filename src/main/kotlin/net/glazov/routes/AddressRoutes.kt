package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.AddressesDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.model.RegisteredAddressesModel
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.employeesroles.EmployeeRoles

private const val PATH = "/api/address-info"

fun Route.addressRoutes(
    addresses: AddressesDataSource,
    employees: EmployeesDataSource
) {
    authenticate("employee") {

        get("$PATH/cities-list") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.ADDRESSES)) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }
            val city = call.request.queryParameters["city"] ?: ""
            val citiesList = addresses.getCitiesNames(city)
            val formattedCitiesList = citiesList.map { it.replaceFirstChar { it.uppercaseChar() } }
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "streets retrieved",
                    data = formattedCitiesList
                )
            )
        }

        get("$PATH/streets-list") {
            val employeeId = call.request.headers["employee_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            if (!employees.checkEmployeeRole(employeeId, EmployeeRoles.ADDRESSES)) {
                call.respond(HttpStatusCode.Forbidden)
                return@get
            }
            val city = call.request.queryParameters["city"]
            val street = call.request.queryParameters["street"]
            if (city !== null && street !== null) {
                val streetsList = addresses.getStreetsForCity(city, street)
                val formattedStreetsNames = streetsList.map {
                    it.street.replaceFirstChar { it.uppercaseChar() }
                }
                call.respond(
                    SimpleResponse(
                        status = true,
                        message = "streets retrieved",
                        data = formattedStreetsNames
                    )
                )
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("$PATH/addresses") {

            val city = call.request.queryParameters["city"]
            val street = call.request.queryParameters["street"]
            if (city != null && street != null) {
                val addressesList = addresses.getAddresses(city, street)
                val formattedAddresses = addressesList.map {
                    it.copy(
                        city = it.city.replaceFirstChar { it.uppercaseChar() },
                        street = it.street.replaceFirstChar { it.uppercaseChar() }
                    )
                }
                call.respond(
                    message = SimpleResponse(
                        status = true,
                        message = "addresses retrieved",
                        data = formattedAddresses
                    ),
                    status = HttpStatusCode.OK
                )
            } else call.respond(
                message = SimpleResponse(
                    status = true,
                    message = "addresses retrieved",
                    data = emptyList<List<RegisteredAddressesModel>>()
                )
            )
        }
    }
}