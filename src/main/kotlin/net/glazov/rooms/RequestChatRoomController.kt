package net.glazov.rooms

import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.glazov.data.datasource.AdminsDataSource
import net.glazov.data.datasource.ChatDataSource
import net.glazov.data.datasource.ClientsDataSource
import net.glazov.data.model.requests.MessageModel
import net.glazov.data.utils.notificationsmanager.NotificationsManager
import java.util.concurrent.ConcurrentHashMap

class RequestChatRoomController(
    private val chat: ChatDataSource,
    private val clients: ClientsDataSource,
    private val admins: AdminsDataSource,
    private val notificationsManager: NotificationsManager
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
                    sendPushNotification(
                        requestId = requestId,
                        senderId = senderId,
                        currentMembersInChat = request.map { it.key }
                    )
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


    //TODO rework notifications logic. Maybe replace to other file
    private suspend fun sendPushNotification(
        requestId: String,
        senderId: String,
        currentMembersInChat: List<String>
    ) {
        val request = chat.getRequestById(requestId) ?: return
        val isNotificationsEnabled = request.isNotificationsEnabled
        val isSendByRequestCreator = request.creatorId == senderId
        val isOwnerOnline = currentMembersInChat.contains(request.creatorId)
        if (!isSendByRequestCreator && !isOwnerOnline && isNotificationsEnabled) {
            val clientFcmToken = clients.getClientById(request.creatorId)?.fcmToken ?: return
            notificationsManager.sendNotificationToClient(
                clientToken = clientFcmToken,
                title = request.title,
                body = "У вас новое сообщение в чате!"
            )
        }
    }

}