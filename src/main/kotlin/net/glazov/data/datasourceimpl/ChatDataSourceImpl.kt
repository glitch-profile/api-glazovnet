package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.ChatDataSource
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.model.requests.MessageModel
import net.glazov.data.model.requests.RequestsStatus
import net.glazov.data.model.requests.RequestsStatus.Companion.convertToIntCode
import net.glazov.data.model.requests.SupportRequestModel
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId

class ChatDataSourceImpl(
    db: MongoDatabase,
    private val clients: ClientsDataSource,
    private val persons: PersonsDataSource
): ChatDataSource {

    private val requests = db.getCollection<SupportRequestModel>("SupportRequests")

    override suspend fun getAllRequests(statuses: List<RequestsStatus>?): List<SupportRequestModel> {
        val requestsList = requests.find().toList()
        val convertedStatus = statuses?.map { it.convertToIntCode() }
        val filteredRequests = if (convertedStatus != null) {
            requestsList.asSequence()
                .filter { request ->
                    convertedStatus.any { request.status == it }
                }
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
        val filter = Filters.eq(SupportRequestModel::creatorClientId.name, clientId)
        return requests.find(filter).toList().sortedByDescending { it.creationDate }
    }

    override suspend fun createNewRequest(
        clientId: String,
        title: String,
        text: String,
        isNotificationEnabled: Boolean
    ): SupportRequestModel? {
        val associatedPersonId = clients.getAssociatedPerson(clientId)?.id ?: return null
        val creatorName = persons.getNameById(associatedPersonId, useShortForm = false)
        val requestToInsert = SupportRequestModel(
            creatorPersonId = associatedPersonId,
            creatorClientId = clientId,
            creatorName = creatorName,
            title = title,
            description = text,
            isNotificationsEnabled = isNotificationEnabled
        )
        val status = requests.insertOne(requestToInsert)
        return if (status.insertedId != null) requestToInsert else null
    }

    override suspend fun getRequestById(requestId: String): SupportRequestModel {
        val filter = Filters.eq("_id", requestId)
        return requests.find(filter).singleOrNull() ?: throw RequestNotFoundException()
    }

    override suspend fun addMessageToRequest(requestId: String, newMessage: MessageModel): MessageModel {

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
        } else throw RequestNotFoundException()
    }

    override suspend fun closeRequest(requestId: String): Boolean {
        val filter = Filters.eq("_id", requestId)
        val update = Updates.set(SupportRequestModel::status.name, RequestsStatus.Solved.convertToIntCode())
        val status = requests.updateOne(filter, update)
        if (status.matchedCount == 0L) throw RequestNotFoundException()
        return status.modifiedCount != 0L
    }

    override suspend fun reopenRequest(requestId: String): Boolean {
        val currentDateTimestamp = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
        val filter = Filters.eq("_id", requestId)
        val update = Updates.combine(
            Updates.set(SupportRequestModel::status.name, RequestsStatus.Active.convertToIntCode()),
            Updates.set(SupportRequestModel::associatedSupportId.name, null),
            Updates.set(SupportRequestModel::reopenDate.name, currentDateTimestamp)
        )
        val status = requests.updateOne(filter, update)
        if (status.matchedCount == 0L) throw RequestNotFoundException()
        return status.modifiedCount != 0L
    }

    override suspend fun changeRequestHelper(requestId: String, newSupportId: String): Boolean {
        val filter = Filters.eq("_id", requestId)
        val update = Updates.combine(
            Updates.set(SupportRequestModel::associatedSupportId.name, newSupportId),
            Updates.set(SupportRequestModel::status.name, RequestsStatus.InProgress.convertToIntCode())
        )
        val status = requests.updateOne(filter = filter, update = update)
        if (status.matchedCount == 0L) throw RequestNotFoundException()
        return status.modifiedCount != 0L
    }

    override suspend fun deleteRequest(requestId: String): Boolean {
        val filter = Filters.eq("_id", requestId)
        return requests.deleteOne(filter).wasAcknowledged()
    }
}

class RequestNotFoundException: Exception(
    "Request with given ID is not found"
)