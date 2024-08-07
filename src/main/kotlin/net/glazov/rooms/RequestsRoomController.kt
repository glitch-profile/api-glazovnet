package net.glazov.rooms

import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.glazov.data.model.requests.SupportRequestModel
import java.util.concurrent.ConcurrentHashMap

class RequestsRoomController {

    private val members = ConcurrentHashMap<String, RequestsMember>()

    fun onJoin(
        memberId: String,
        socket: WebSocketSession
    ) {
        if (members.containsKey(memberId)) {
            throw MemberAlreadyExistException()
        } else {
            members[memberId] = RequestsMember(
                memberId = memberId,
                socket = socket
            )
        }
    }

    suspend fun sendRequestToSocket(
        request: SupportRequestModel
    ) {
        val json = Json {
            encodeDefaults = true
        }
        val parsedRequest = json.encodeToString(value = request)
        members.values.forEach {member ->
            member.socket.send(Frame.Text(parsedRequest))
        }
    }

    suspend fun tryDisconnect(memberId: String) {
        members[memberId]?.socket?.close()
        if (members.containsKey(memberId)) {
            members.remove(memberId)
        }
    }
}