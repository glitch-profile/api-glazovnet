package net.glazov.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.model.auth.AuthModel
import net.glazov.data.model.auth.AuthResponseModel
import net.glazov.data.model.response.SimpleResponse
import java.time.OffsetDateTime
import java.time.ZoneId

private const val PATH = "/api"

fun Routing.authRoutes(
    persons: PersonsDataSource,
    clients: ClientsDataSource,
    employees: EmployeesDataSource
) {

    val issuer = ApplicationConfig(null).tryGetString("auth.issuer").toString()
    val secret = ApplicationConfig(null).tryGetString("auth.secret").toString()

    post("$PATH/login") {
        val authData = call.receiveNullable<AuthModel>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }
        val person = persons.login(authData.username, authData.password) ?: kotlin.run {
            call.respond(
                SimpleResponse(
                    status = false,
                    message = "user not found",
                    data = null
                )
            )
            return@post
        }
        val associatedClient = clients.getClientByPersonId(person.id)
        val associatedEmployee = employees.getEmployeeByPersonId(person.id)
        val expireDateInstant = OffsetDateTime.now(ZoneId.systemDefault()).plusMonths(6).toInstant()
        val token = JWT.create()
            .withIssuer(issuer)
            .withClaim("person_id", person.id)
            .withClaim("client_id", associatedClient?.id)
            .withClaim("employee_id", associatedEmployee?.id)
            .withExpiresAt(expireDateInstant)
            .sign(Algorithm.HMAC256(secret))
        call.respond(
            AuthResponseModel(
                token = token,
                personId = person.id,
                clientId = associatedClient?.id,
                employeeId = associatedEmployee?.id
            )
        )
    }

}