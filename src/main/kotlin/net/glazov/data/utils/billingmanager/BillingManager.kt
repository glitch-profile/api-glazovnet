package net.glazov.data.utils.billingmanager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.glazov.data.datasource.ServicesDataSource
import net.glazov.data.datasource.TariffsDataSource
import net.glazov.data.datasource.users.ClientsDataSource
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

private const val TAG = "BILLING MANAGER"
private const val DAY_LENGTH_IN_SECONDS = 86400

// TODO: Add cache for tariffs and services. It takes to much time to calculate payment amount for a single client
class BillingManager(
    private val clients: ClientsDataSource,
    private val tariffs: TariffsDataSource,
    private val services: ServicesDataSource
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default + Job())

    private val timer = Timer()
    private val task: TimerTask = object: TimerTask() {
        override fun run() {
            simulateBilling()
        }
    }

    fun init() {
        val currentDateTime = LocalDateTime.now()
        val defaultZone = ZoneId.systemDefault()
        val isCanInitToday = currentDateTime.hour < 12 // payment is always calculated at 12:00 every day
        val nearestInitDate = if (isCanInitToday) currentDateTime.toLocalDate().atTime(12, 0, 0)
        else currentDateTime.toLocalDate().plusDays(1).atTime(12, 0, 0)
        println("$TAG: nearest simulation start date = $nearestInitDate")
        val timeDifferenceInSeconds = nearestInitDate.atZone(defaultZone).toEpochSecond() - currentDateTime.atZone(defaultZone).toEpochSecond()
        val timerPeriod = (DAY_LENGTH_IN_SECONDS * 1000).toLong()  // to milliseconds
        timer.schedule(task, timeDifferenceInSeconds * 1000, timerPeriod)
    }

    private fun simulateBilling() {
        coroutineScope.launch {
            println("$TAG: calculation of monthly bill payments has begun")
            val currentDateTime = LocalDate.now().atTime(12, 0, 0).atZone(ZoneId.systemDefault())
            val nextBillingDate = currentDateTime.plusMonths(1).toEpochSecond()
            val clientsForPayment = clients.getClientsForBillingDate(currentDateTime.toEpochSecond())
            clientsForPayment.forEach { client ->
                var amountToPay = 0
                val connectedTariffId = client.pendingTariffId ?: client.tariffId
                val connectedServices = services.getMultipleServicesById(client.connectedServices)
                val connectedTariff = tariffs.getTariffById(connectedTariffId)
                if (connectedTariff != null) amountToPay += connectedTariff.costPerMonth
                else println("$TAG: unable to find tariff with id: $connectedTariffId")
                connectedServices.forEach { service ->
                    if (service.isActive) amountToPay += service.costPerMonth
                }
                clients.initStartOfBillingMonth(
                    clientId = client.id,
                    nextBillingDate = nextBillingDate,
                    paymentAmount = amountToPay
                )
            }
            println("$TAG: calculation of monthly bill payments is over")
        }
    }

}