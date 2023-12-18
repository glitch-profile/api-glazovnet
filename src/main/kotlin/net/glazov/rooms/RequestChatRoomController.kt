package net.glazov.rooms

import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.glazov.data.model.MessageModel
import java.util.concurrent.ConcurrentHashMap

class RequestChatRoomController {

    private val requests = ConcurrentHashMap<String, ConcurrentHashMap<String, Member>>()

    fun onJoin(
        requestId: String,
        memberId: String,
        memberSocket: WebSocketSession
    ) {
        println("new member connecting to chat")
        if (requests[requestId]?.containsKey(memberId) == true) {
            throw MemberAlreadyExistException()
        } else {
            if (requests.containsKey(requestId)) {
                requests[requestId]!!.put(memberId, Member(memberId, memberSocket))
            } else {
                requests.put(
                    key = requestId,
                    value = ConcurrentHashMap<String, Member>()
                )
                requests[requestId]!!.put(memberId, Member(memberId, memberSocket))
            }
        }
        println("person connected to room $requestId\nRequest rooms - ${requests.keys().toList().joinToString(", ")}\nCurrent room users - ${requests[requestId]?.values?.joinToString(", ")}]")
    }

    suspend fun sendMessage(
        requestId: String,
        messageToSend: MessageModel
    ) {
        requests[requestId]?.let {request ->
            val json = Json {
                encodeDefaults = true
            }
            val encodedMessage = json.encodeToString(messageToSend)
            request.values.forEach { member ->
                member.socket.send(Frame.Text(encodedMessage))
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
        println("user disconnected\ncurrent rooms - ${requests.keys().toList().joinToString(", ")}")
    }

}