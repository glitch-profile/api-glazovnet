package net.glazov.rooms

import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.glazov.data.datasource.ChatDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.model.requests.MessageModel
import net.glazov.data.utils.notificationsmanager.Deeplink
import net.glazov.data.utils.notificationsmanager.NotificationChannel
import net.glazov.data.utils.notificationsmanager.NotificationsManager
import net.glazov.data.utils.notificationsmanager.TranslatableNotificationData
import java.util.concurrent.ConcurrentHashMap

class RequestChatRoomController(
    private val chat: ChatDataSource,
    private val persons: PersonsDataSource,
    private val notificationsManager: NotificationsManager
) {

    private val requests = ConcurrentHashMap<String, ConcurrentHashMap<String, ChatMember>>()

    suspend fun onJoin(
        requestId: String,
        personId: String,
        memberSocket: WebSocketSession
    ) {
        if (requests[requestId]?.containsKey(personId) == true) {
            throw MemberAlreadyExistException()
        } else {
            if (!requests.containsKey(requestId)) {
                requests.put(
                    key = requestId,
                    value = ConcurrentHashMap<String, ChatMember>()
                )
            }
            val memberName = persons.getNameById(personId, true)
            requests[requestId]!!.put(
                personId,
                ChatMember(
                    memberId = personId,
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
                        messageText = message,
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
        messageText: String,
        currentMembersInChat: List<String>
    ) {
        val request = chat.getRequestById(requestId) ?: return
        val isNotificationsEnabled = request.isNotificationsEnabled
        val isSendByRequestCreator = request.creatorId == senderId
        val isOwnerOnline = currentMembersInChat.contains(request.creatorId)
        if (!isSendByRequestCreator && !isOwnerOnline && isNotificationsEnabled) {
            val clientFcmToken = persons.getPersonById(request.creatorId)?.fcmTokensList ?: return
            notificationsManager.sendTranslatableNotificationByTokens(
                personsTokensList = listOf(clientFcmToken),
                translatableData = TranslatableNotificationData.NewChatMessage(
                    requestTitle = request.title,
                    messageText = messageText
                ),
                notificationChannel = NotificationChannel.Chat,
                deepLink = Deeplink.SupportChat(requestId)
            )
        }
    }

}