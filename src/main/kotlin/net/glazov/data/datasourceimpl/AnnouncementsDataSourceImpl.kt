package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.AnnouncementsDataSource
import net.glazov.data.model.AnnouncementModel
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AnnouncementsDataSourceImpl(
    private val db: MongoDatabase,
    private val clients: ClientsDataSourceImpl
): AnnouncementsDataSource {

    private val announcements = db.getCollection<AnnouncementModel>("Announcements")

    override suspend fun getAnnouncements(): List<AnnouncementModel> {
        return announcements.find().toList().reversed()
    }

    override suspend fun getAnnouncementList(limit: Int, offset: Int): List<AnnouncementModel> {
        val announcements = announcements.find().toList().reversed()
        return announcements.subList(offset, offset + limit)
    }

    override suspend fun getAnnouncementByClientId(clientId: String): List<AnnouncementModel> {
        val client = clients.getClientById(clientId)
        return if (client != null) {
            val address = client.address
            val announcement = getAnnouncementsByAddress(
                city = address.cityName,
                street = address.streetName,
                houseNumber = address.houseNumber
            )
            announcement
        } else {
            emptyList()
        }
    }

    override suspend fun getAnnouncementsByAddress(
        city: String,
        street: String,
        houseNumber: String
    ): List<AnnouncementModel> {
        val announcements = announcements.find().toList().asReversed()
        return announcements.filter { it.isContainingAddress(city, street, houseNumber) }
    }

    override suspend fun addAnnouncement(announcement: AnnouncementModel): AnnouncementModel? {
        val announcementToInsert = announcement.copy(
            id = ObjectId().toString(),
            creationDate = OffsetDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME)
        )
        val status = announcements.insertOne(announcementToInsert).wasAcknowledged()
        return if (status) announcementToInsert else null
    }

    override suspend fun deleteAnnouncement(announcementId: String): Boolean {
        val filter = Filters.eq("_id", announcementId)
        val deletedAnnouncement = announcements.findOneAndDelete(filter)
        return deletedAnnouncement != null
    }

    override suspend fun updateAnnouncement(newAnnouncement: AnnouncementModel): Boolean {
        val filter = Filters.eq("_id", newAnnouncement.id)
        val updatedAnnouncement = announcements.findOneAndReplace(filter, newAnnouncement)
        return updatedAnnouncement != null
    }
}