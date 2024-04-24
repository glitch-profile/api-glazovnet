package net.glazov.data.datasource

import net.glazov.data.model.TransactionModel
import java.time.OffsetDateTime
import java.time.ZoneId

interface TransactionsDataSource {

    suspend fun addTransaction(
        clientId: String,
        amount: Float,
        isIncoming: Boolean,
        note: String? = null,
        transactionTimestamp: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
    ): TransactionModel?

    suspend fun getTransactionsForClientId(
        clientId: String
    ): List<TransactionModel>

    suspend fun getTransactionById(id: String): TransactionModel?

}