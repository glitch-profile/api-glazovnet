package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.ChatDataSource
import net.glazov.data.model.MessageModel
import net.glazov.data.model.SupportRequestModel
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId

class ChatDataSourceImpl(
    private val db: MongoDatabase
): ChatDataSource {

    private val requests = db.getCollection<SupportRequestModel>("SupportRequests")

    override suspend fun getAllRequests(status: Int?): List<SupportRequestModel> {
        val requestsList = requests.find().toList()
        val filteredRequests = if (status != null) {
            requestsList.asSequence()
                .filter { it.status == status }
                .sortedByDescending { it.creationDate }
                .map { it.copy(messages = emptyList()) }
                .toList()
        } else {
            requestsList.asSequence()
                .sortedByDescending { it.creationDate }
                .map { it.copy(messages = emptyList()) }
                .toList()
        }
        return filteredRequests
    }

    override suspend fun getRequestsForClient(clientId: String): List<SupportRequestModel> {
        TODO("Not yet implemented")
    }

    override suspend fun createNewRequest(newRequest: SupportRequestModel): SupportRequestModel? {
        val requestToInsert = newRequest.copy(
            id = ObjectId().toString(),
            creationDate = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond(),
            associatedSupportId = null,
            messages = emptyList()
        )
        val status = requests.insertOne(requestToInsert).wasAcknowledged()
        return if (status) requestToInsert else null
    }

    override suspend fun getRequestById(requestId: String): SupportRequestModel? {
        val filter = Filters.eq("_id", requestId)
        return requests.find(filter).singleOrNull()
    }

    override suspend fun addMessageToRequest(requestId: String, newMessage: MessageModel): MessageModel? {

        val requestFilter = Filters.eq("_id", requestId)
        val request = requests.find(requestFilter).singleOrNull()
        return if (request != null) {
            val message = newMessage.copy(
                id = ObjectId().toString(),
                timestamp = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
            )
            val newMessagesList = request.messages.toMutableList()
            newMessagesList.add(0, message)
            val newRequest = request.copy(messages = newMessagesList)
            requests.findOneAndReplace(requestFilter, newRequest)
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

class RequestNotFoundException: Exception(
    "Request with given ID is not found"
)