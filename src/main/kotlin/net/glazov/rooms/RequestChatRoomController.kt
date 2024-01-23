package net.glazov.rooms

import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.glazov.data.datasource.AdminsDataSource
import net.glazov.data.datasource.ChatDataSource
import net.glazov.data.datasource.ClientsDataSource
import net.glazov.data.model.requests.MessageModel
import java.util.concurrent.ConcurrentHashMap

class RequestChatRoomController(
    private val chat: ChatDataSource,
    private val clients: ClientsDataSource,
    private val admins: AdminsDataSource
) {

    private val requests = ConcurrentHashMap<String, ConcurrentHashMap<String, ChatMember>>()

    suspend fun onJoin(
        requestId: String,
        memberId: String,
        isAdmin: Boolean,
        memberSocket: WebSocketSession
    ) {
        if (requests[requestId]?.containsKey(memberId) == true) {
            throw MemberAlreadyExistException()
        } else {
            if (!requests.containsKey(requestId)) {
                requests.put(
                    key = requestId,
                    value = ConcurrentHashMap<String, ChatMember>()
                )
            }
            val memberName = if (isAdmin) admins.getAdminNameById(memberId, true)
            else clients.getClientNameById(memberId, true)
            requests[requestId]!!.put(
                memberId,
                ChatMember(
                    memberId = memberId,
                    memberName = memberName,
                    isAdmin = isAdmin,
                    socket = memberSocket
                )
            )
        }
    }

    suspend fun sendMessage(
        requestId: String,
        senderId: String,
        message: String
    ) {
        requests[requestId]?.let {request ->
            request[senderId]?.let { sender ->
                val messageToSend = chat.addMessageToRequest(
                    requestId = requestId,
                    MessageModel(
                        senderId = senderId,
                        isAdmin = sender.isAdmin,
                        senderName = sender.memberName,
                        text = message,
                        timestamp = 0L
                    )
                )
                if (messageToSend != null) {
                    val json = Json {
                        encodeDefaults = true
                    }
                    val encodedMessage = json.encodeToString(messageToSend)
                    request.values.forEach { member ->
                        member.socket.send(Frame.Text(encodedMessage))
                    }
                }
            }
        }
    }

    suspend fun tryDisconnect(
        requestId: String,
        memberId: String
    ) {
        requests[requestId]?.let {request ->
            request[memberId]?.socket?.close()
            if (request.containsKey(memberId)) {
                request.remove(memberId)
            }
            if (request.values.isEmpty()) {
                requests.remove(requestId)
            }
        }
    }

}