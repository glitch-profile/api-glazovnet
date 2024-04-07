package net.glazov.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import net.glazov.data.model.response.SimpleResponse
import net.glazov.data.utils.UrlChanger
import net.glazov.data.utils.filemanager.FileManager
import java.io.File
import kotlin.io.path.Path

private const val PATH = "/api/utils"
private const val MAX_CONTENT_LENGTH = 10_485_760 //10Mb in bytes

fun Routing.utilRoutes(
    fileManager: FileManager
) {
    authenticate {

        get("$PATH/get-file") {
            val filePath = call.request.queryParameters["path"]
            if (filePath != null) {
                val file = fileManager.getFile(localStaticPath = filePath)
                if (file != null) {
                    call.respondFile(file)
                } else call.respond(HttpStatusCode.NotFound)
            } else call.respond(HttpStatusCode.BadRequest)
        }
    }

    authenticate("admin") {

        post("$PATH/upload-files") {
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
        }
    }

    get("$PATH/get-intro-image-url") {
        val imagesDirectoryPath = Path("").toAbsolutePath().toString() + "\\static\\images\\intro"
        val directory = File(imagesDirectoryPath)
        val imagesList = directory.listFiles()
        if (imagesList?.isNotEmpty() == true) {
            val image = imagesList.random().absolutePath

            //TODO: remove after set up stable server
            val replacedImagePath = UrlChanger.toCurrentUrl(fileManager.getLocalPath(image))
            call.respond(replacedImagePath)

//            call.respond(fileManager.getLocalPath(image))
        } else call.respond("")
    }
}