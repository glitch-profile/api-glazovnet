package net.glazov.data.datasource

import net.glazov.data.model.MessageModel
import net.glazov.data.model.SupportRequestModel

interface ChatDataSource {

    suspend fun getAllRequests(
        showOnlyActiveRequests: Boolean = true
    ): List<SupportRequestModel>

    suspend fun getRequestsForClient(
        clientId: String
    ): List<SupportRequestModel>

    suspend fun getAllMessagesForRequest(
        requestId: String
    ): List<MessageModel>

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