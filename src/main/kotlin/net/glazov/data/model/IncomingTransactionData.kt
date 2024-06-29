package net.glazov.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IncomingTransactionData(
    val amount: Float,
    val note: String?
)
