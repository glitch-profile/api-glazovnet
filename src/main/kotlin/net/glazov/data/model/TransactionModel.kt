package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class TransactionModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val clientId: String,
    val transactionTimestamp: Long,
    val amount: Double,
    val isIncoming: Boolean,
    val note: String?
)
