package net.glazov.data.model.response

import kotlinx.serialization.Serializable
import net.glazov.data.model.AnnouncementModel

@Serializable
data class AnnouncementResponse(
    val status: Boolean,
    val message: String,
    val data: List<AnnouncementModel>
)
