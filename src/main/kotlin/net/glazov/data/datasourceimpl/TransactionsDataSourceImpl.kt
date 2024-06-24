package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.TransactionsDataSource
import net.glazov.data.model.TransactionModel
import net.glazov.data.utils.paymentmanager.TransactionNoteTextCode
import org.bson.types.ObjectId

class TransactionsDataSourceImpl(
    db: MongoDatabase
): TransactionsDataSource {

    private val transactions = db.getCollection<TransactionModel>("Transactions")

    override suspend fun addTransaction(
        clientId: String,
        amount: Float,
        isIncoming: Boolean,
        note: TransactionNoteTextCode?,
        transactionTimestamp: Long
    ): TransactionModel? {
        val transactionModel = TransactionModel(
            id = ObjectId().toString(),
            clientId = clientId,
            transactionTimestamp = transactionTimestamp,
            amount = amount,
            isIncoming = isIncoming,
            note = note?.code
        )
        val addResult = transactions.insertOne(document = transactionModel)
        return if (addResult.insertedId !== null) transactionModel
        else null
    }

    override suspend fun getTransactionsForClient(clientId: String): List<TransactionModel> {
        val filter = Filters.eq("_id", clientId)
        return transactions.find(filter).toList().sortedByDescending { it.transactionTimestamp }
    }

    override suspend fun getTransactionsForClient(
        clientId: String,
        startTimestamp: Int?,
        endTimestamp: Int?
    ): List<TransactionModel> {
        val filter = Filters.and(
            Filters.eq("_id", clientId),
            if (startTimestamp != null) Filters.gte(TransactionModel::transactionTimestamp.name, startTimestamp) else Filters.empty(),
            if (endTimestamp != null) Filters.lte(TransactionModel::transactionTimestamp.name, endTimestamp) else Filters.empty()
        )
        return transactions.find(filter).toList().sortedByDescending { it.transactionTimestamp }
    }

    override suspend fun getTransactionById(id: String): TransactionModel? {
        val filter = Filters.eq("_id", id)
        return transactions.find(filter).toList().singleOrNull()
    }
}