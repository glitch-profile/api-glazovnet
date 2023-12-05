package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import net.glazov.data.datasource.ChatDataSource
import net.glazov.data.model.MessageModel
import net.glazov.data.model.SupportRequestModel
import java.time.OffsetDateTime
import java.time.ZoneId

class ChatDataSourceImpl(
    private val db: MongoDatabase
): ChatDataSource {

    private val requests = db.getCollection<SupportRequestModel>("SupportRequests")

    override suspend fun getAllRequests(showOnlyActiveRequests: Boolean): List<SupportRequestModel> {
        TODO("Not yet implemented")
    }

    override suspend fun getRequestsForClient(login: String, password: String): List<SupportRequestModel> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllMessagesForRequest(requestId: String): List<MessageModel> {
        val filter = Filters.eq("_id", requestId)
        val request = requests.find(filter).singleOrNull()
        return request?.messages?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    override suspend fun createNewRequest(newRequest: SupportRequestModel): SupportRequestModel? {
        TODO("Not yet implemented")
    }

    override suspend fun addMessageToRequest(requestId: String, newMessage: MessageModel): MessageModel? {

        val requestFilter = Filters.eq("_id", requestId)
        val request = requests.find(requestFilter).singleOrNull()
        return if (request != null) {
            val message = newMessage.copy(
                timestamp = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
            )
            val newMessagesList = request.messages.toMutableList()
            newMessagesList.add(0, message)
            val newRequest = request.copy(messages = newMessagesList)
            requests.insertOne(newRequest)
            message
        } else null
    }

    override suspend fun markRequestAsSolved(requestId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRequest(requestId: String): Boolean {
        TODO("Not yet implemented")
    }
}