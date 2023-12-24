package net.glazov.data.utils

import java.io.File

enum class FileExtension(
    val extensionsList: List<String>,
    val contentPath: String
) {
    IMAGE(
        listOf("jpg", "jpeg", "png", "heif", "webp", "svg"),
        "/images"
    ),
    VIDEO(
        listOf("mp4", "avi", "mov"),
        "/videos"

    ),
    DOCUMENT(
        listOf("pdf", "doc", "docx", "txt"),
        "/documents"
    ),
    OTHER(
        emptyList(),
        "/other"
    )
}

fun File.getExtension(): FileExtension {
    val extensionString = this.extension.lowercase()
    FileExtension.entries.forEach { extension ->
        if (extension.extensionsList.contains(extensionString)) return extension
    }
    return FileExtension.OTHER
}