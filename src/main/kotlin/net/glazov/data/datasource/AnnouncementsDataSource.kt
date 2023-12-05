package net.glazov.data.datasource

import net.glazov.data.model.AnnouncementModel

interface AnnouncementsDataSource {

    suspend fun getAnnouncements(): List<AnnouncementModel>

    suspend fun getAnnouncementList(
        limit: Int = 20,
        offset: Int = 0
    ): List<AnnouncementModel>

    suspend fun getAnnouncementForClient(
        login: String,
        password: String
    ): List<AnnouncementModel>

    suspend fun addAnnouncement(
        announcement: AnnouncementModel
    ): AnnouncementModel?

    suspend fun deleteAnnouncement(
        announcementId: String
    ): Boolean

    suspend fun updateAnnouncement(
        newAnnouncement: AnnouncementModel
    ): Boolean

}