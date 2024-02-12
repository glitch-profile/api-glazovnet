package net.glazov.data.utils

import java.io.File

interface FileManager {

    fun uploadFile(fileName: String, fileBytes: ByteArray): String

    fun getFile(localStaticPath: String): File?

    fun getAbsolutePath(localPath: String): String

    fun getLocalPath(absolutePath: String): String

}