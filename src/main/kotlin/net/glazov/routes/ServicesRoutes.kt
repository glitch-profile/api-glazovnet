package net.glazov.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.glazov.data.datasource.ServicesDataSource
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.model.response.SimpleResponse

private const val PATH = "/api/services"

fun Route.servicesRoutes(
    services: ServicesDataSource,
    clients: ClientsDataSource
) {

    authenticate {

        get(PATH) {
            val result = services.getAllServices()
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
            val result = services.getServiceById(serviceId) ?: kotlin.run {
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

    authenticate("client") {

        get("$PATH/connected-for-client") {
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

        get("$PATH/connected-for-client/ids") {
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
                    message = "services retrieved",
                    data = clientInfo.connectedServices
                )
            )
        }

        put("$PATH/connect-client-service") {
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

        put("$PATH/disconnect-client-service") {
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
            val result = clients.disconnectService(clientId, serviceId)
            call.respond(
                SimpleResponse(
                    status = result,
                    message = if (result) "service disconnected" else "unable to disconnect this service",
                    data = Unit
                )
            )
        }

    }

}