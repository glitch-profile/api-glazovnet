package net.glazov.data.datasource

import net.glazov.data.model.AnnouncementModel
import net.glazov.data.model.ClientModel

interface AnnouncementsDataSource {

    suspend fun getAnnouncements(): List<AnnouncementModel>

    suspend fun getAnnouncementList(
        limit: Int = 20,
        offset: Int = 0
    ): List<AnnouncementModel>

    suspend fun getAnnouncementForClient(
        clientId: String,
        isAdmin: Boolean
    ): List<AnnouncementModel>

    suspend fun getClientsForAnnouncement(
        announcement: AnnouncementModel
    ): List<ClientModel>

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