package net.glazov.data.utils

import java.io.File
import java.nio.file.Paths

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

    override fun getFile(localPath: String): File? {
        TODO("Not yet implemented")
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
        ).replace("\\", "/")
    }
}