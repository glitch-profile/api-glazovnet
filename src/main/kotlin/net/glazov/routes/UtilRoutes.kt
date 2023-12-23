package net.glazov.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import net.glazov.data.model.response.SimpleResponse
import java.io.File
import java.nio.file.Paths

private const val PATH = "/api/utils"
private const val BASE_URL = "http://82.179.120.25:8080/"

fun Routing.utilRoutes(
    apiKeyServer: String
) {
    post("$PATH/upload-image") {
        val apiKey = call.request.headers["api_key"]
        if (apiKey == apiKeyServer) {
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
                            val path = Paths.get("").toAbsolutePath().toString() //getting local path to static folder
                            val file = File("$path/static/images", fileName)
                            file.writeBytes(fileBytes)
                            println(file.path)
                            val localFilePath = file.relativeTo(File("$path/static"))
                                .path
                                .replace("\\", "/")
                            paths.add(BASE_URL + localFilePath)
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
        } else call.respond(HttpStatusCode.Forbidden)
    }

}