package net.glazov.data.response

import kotlinx.serialization.Serializable
import net.glazov.data.model.AnnouncementModel

@Serializable
data class AnnouncementResponse(
    val status: Boolean,
    val message: String,
    val data: List<AnnouncementModel>
)
