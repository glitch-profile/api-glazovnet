package net.glazov.plugins

import io.ktor.server.application.*
import net.glazov.data.datasource.ServicesDataSource
import net.glazov.data.datasource.TariffsDataSource
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.utils.billingmanager.BillingManager
import org.koin.ktor.ext.inject

fun Application.configureBillingSimulation() {
    val clients by inject<ClientsDataSource>()
    val tariffs by inject<TariffsDataSource>()
    val services by inject<ServicesDataSource>()

    val billingManager = BillingManager(clients, tariffs, services)
    billingManager.init()
}