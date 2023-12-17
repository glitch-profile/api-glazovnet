package net.glazov.rooms

import io.ktor.websocket.*
import net.glazov.data.model.MessageModel
import java.util.concurrent.ConcurrentHashMap

class RequestsChatRoomController {

    private val requests = ConcurrentHashMap<String, ConcurrentHashMap<String, Member>>()

    fun onJoin(
        requestId: String,
        memberId: String,
        memberSocket: WebSocketSession
    ) {

    }

    fun onSendMessage(
        requestId: String,
        messageToSend: MessageModel
    ) {

    }

    suspend fun onMemberLeave(
        requestId: String,
        memberId: String
    ) {
        requests[requestId]?.let {request ->
            request[memberId]?.socket?.close()
            if (request.containsKey(memberId)) {
                request.remove(memberId)
            }
            if (request.isEmpty()) {
                requests.remove(requestId)
            }
        }
    }

}