package net.glazov.data.datasource

import net.glazov.data.model.requests.MessageModel
import net.glazov.data.model.requests.RequestsStatus
import net.glazov.data.model.requests.SupportRequestModel

interface ChatDataSource {

    suspend fun getAllRequests(
        statuses: List<RequestsStatus>? = null
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

    suspend fun changeRequestStatus(
        requestId: String,
        newStatus: Int
    ): Boolean

    suspend fun changeRequestHelper(
        requestId: String,
        newSupportId: String?
    ): Boolean

    suspend fun deleteRequest(
        requestId: String
    ): Boolean

}