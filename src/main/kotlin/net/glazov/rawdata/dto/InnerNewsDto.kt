package net.glazov.rawdata.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InnerNewsDto(
    @SerialName("Id")
    val id: String,
    @SerialName("Title")
    val title: String, //"", if it should be null
    @SerialName("Text")
    val text: String,
    @SerialName("DateCreation")
    val dateTimestamp: Long
)
