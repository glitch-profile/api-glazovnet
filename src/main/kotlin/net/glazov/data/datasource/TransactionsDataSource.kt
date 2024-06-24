package net.glazov.data.datasource

import net.glazov.data.model.TransactionModel
import net.glazov.data.utils.paymentmanager.TransactionNoteTextCode
import java.time.OffsetDateTime
import java.time.ZoneId

interface TransactionsDataSource {

    suspend fun addTransaction(
        clientId: String,
        amount: Float,
        isIncoming: Boolean,
        note: TransactionNoteTextCode? = null,
        transactionTimestamp: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
    ): TransactionModel?

    suspend fun getTransactionsForClient(
        clientId: String
    ): List<TransactionModel>

    suspend fun getTransactionsForClient(
        clientId: String,
        startTimestamp: Int?,
        endTimestamp: Int?
    ): List<TransactionModel>

    suspend fun getTransactionById(id: String): TransactionModel?

}