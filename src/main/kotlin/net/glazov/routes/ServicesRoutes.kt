package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.ServicesDataSource
import net.glazov.data.model.response.SimpleResponse

private const val PATH = "/api/services"

fun Route.servicesRoutes(
    servicesDataSource: ServicesDataSource
) {

    authenticate("client") {

        get(PATH) {
            val result = servicesDataSource.getAllServices()
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "services retrieved",
                    data = result
                )
            )
        }

        get("$PATH/{service_id}") {
            val serviceId = call.parameters["service_id"] ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val result = servicesDataSource.getServiceById(serviceId) ?: kotlin.run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }
            call.respond(
                SimpleResponse(
                    status = true,
                    message = "service retrieved",
                    data = result
                )
            )
        }

    }

}