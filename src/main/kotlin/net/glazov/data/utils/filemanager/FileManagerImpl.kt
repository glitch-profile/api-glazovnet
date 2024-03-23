package net.glazov.data.utils.filemanager

import java.io.File
import java.nio.file.Paths

private const val secondaryUrl = "http://82.179.120.84:8080"

class FileManagerImpl(
    private val baseUrl: String
): FileManager {

    override fun uploadFile(fileName: String, fileBytes: ByteArray): String {
        val localPath = "${Paths.get("").toAbsolutePath()}/static"
        val contentPath = File(fileName).getExtension().contentPath
        val pathToFile = localPath + contentPath
        val file = File(pathToFile, fileName)
        file.writeBytes(fileBytes)
        return getLocalPath(file.absolutePath)
    }

    override fun getFile(localStaticPath: String): File? {
        val absolutePath = "${Paths.get("").toAbsolutePath()}\\static\\$localStaticPath"
        val file = File(absolutePath)
        return if (file.exists() && file.canRead()) {
            file
        } else null
    }

    override fun getAbsolutePath(localPath: String): String {
        val absolutePathBase = "${Paths.get("").toAbsolutePath()}\\static"
        return localPath.replace(
            baseUrl,
            absolutePathBase
        ).replace("/", "\\")
    }

    override fun getLocalPath(absolutePath: String): String {
        val absolutePathBase = "${Paths.get("").toAbsolutePath()}\\static"
        return absolutePath.replace(
            absolutePathBase,
            baseUrl
//            secondaryUrl
        ).replace("\\", "/")
    }
}