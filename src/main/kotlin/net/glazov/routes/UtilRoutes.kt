package net.glazov.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import java.io.File
import java.nio.file.Paths

private const val PATH = "/api/utils"

fun Routing.utilRoutes(
    apiKeyServer: String
) {

    post("$PATH/upload-file") {
        val apiKey = call.request.headers["api_key"]
        if (apiKey == apiKeyServer) {
            try {
                val multipart = call.receiveMultipart()
                multipart.forEachPart {part ->
                    when (part) {
                        is PartData.FormItem -> Unit
                        is PartData.FileItem -> {
                            val fileName = part.originalFileName ?: generateNonce()
                            val fileBytes = part.streamProvider().readBytes()
                            val path = Paths.get("").toAbsolutePath().toString()
                            //val file = File("src/main/resources/static", fileName)
                            val file = File("$path/ImageCache", fileName)
                            file.writeBytes(fileBytes)
                            println(file.absoluteFile.path)
                        }
                        else -> Unit
                    }
                    part.dispose()
                }
                call.respond("file uploaded")
            } catch (e: Exception) {
                call.respond(e.message ?: "ERROR")
            }
        } else call.respond(HttpStatusCode.Forbidden)
    }

}