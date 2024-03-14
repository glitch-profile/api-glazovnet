package net.glazov.data.utils.paymentmanager

import kotlinx.coroutines.delay
import kotlin.random.Random

class TransactionManagerImpl: TransactionManager {

    override suspend fun makeTransaction(): Boolean {
        delay(300L)
        return Random.nextBoolean()
    }
}