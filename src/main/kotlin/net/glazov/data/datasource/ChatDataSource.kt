package net.glazov.data.datasource

import net.glazov.data.model.MessageModel
import net.glazov.data.model.SupportRequestModel

interface ChatDataSource {

    suspend fun getAllRequests(
        status: Int? = null
    ): List<SupportRequestModel>

    suspend fun getRequestsForClient(
        clientId: String
    ): List<SupportRequestModel>

    suspend fun getRequestById(
        requestId: String
    ): SupportRequestModel?

    suspend fun createNewRequest(
        newRequest: SupportRequestModel
    ): SupportRequestModel?

    suspend fun addMessageToRequest(
        requestId: String,
        newMessage: MessageModel
    ): MessageModel?

    suspend fun markRequestAsSolved(
        requestId: String
    ): Boolean

    suspend fun deleteRequest(
        requestId: String
    ): Boolean

}