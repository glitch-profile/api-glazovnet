package net.glazov.data.utils.paymentmanager

interface TransactionManager {

    suspend fun makeTransaction(): Boolean

}