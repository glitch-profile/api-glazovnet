package net.glazov.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.FileManager
import java.io.File
import java.nio.file.Paths

private const val PATH = "/api/utils"
private const val MAX_CONTENT_LENGTH = 10_485_760 //10Mb in bytes

fun Routing.utilRoutes(
    apiKeyServer: String,
    fileManager: FileManager
) {
    post("$PATH/upload-files") {
        val apiKey = call.request.headers["api_key"]
        if (apiKey == apiKeyServer) {
            val requestLength = call.request.headers["content-length"]!!.toLong()
            if ( requestLength <= MAX_CONTENT_LENGTH ) {
                val paths = emptyList<String>().toMutableList()
                try {
                    val multipart = call.receiveMultipart()
                    multipart.forEachPart {part ->
                        when (part) {
                            is PartData.FormItem -> Unit
                            is PartData.FileItem -> {
                                val fileNameFormatted = part.originalFileName?.filter {
                                    it.isLetterOrDigit() || it == '.' || it == '_'
                                }
                                val fileName = fileNameFormatted ?: generateNonce()
                                val fileBytes = part.streamProvider().readBytes()
                                val filePath = fileManager.uploadFile(fileName, fileBytes)
                                paths.add(filePath)
                            }
                            else -> Unit
                        }
                        part.dispose()
                    }
                    call.respond(
                        SimpleResponse(
                            status = true,
                            message = "${paths.size} images uploaded",
                            data = paths
                        )
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        SimpleResponse(
                            status = false,
                            message = e.message ?: "server error",
                            data = emptyList<String>()
                        )
                    )
                }
            } else call.respond(HttpStatusCode.PayloadTooLarge)
        } else call.respond(HttpStatusCode.Forbidden)
    }
}