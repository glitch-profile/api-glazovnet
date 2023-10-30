package net.glazov.database

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.AnnouncementModel
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val mongoUri = ApplicationConfig(null).tryGetString("storage.mongo_db_uri").toString()
private val client = MongoClient.create(mongoUri)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<AnnouncementModel>("Announcements")

suspend fun getAnnouncementList(
    limit: Int = 20,
    offset: Int = 0
): List<AnnouncementModel> {
    val announcements = collection.find().toList().reversed()
    return announcements.subList(offset, offset + limit)
}

suspend fun getAnnouncementByClientId(
    clientId: String
): List<AnnouncementModel> {
    val client = getClientById(clientId)
    if (client != null) {
        val address = client.address
        val announcement = getAnnouncementsByAddress(
            city = address.cityName,
            street = address.streetName,
            houseNumber = address.houseNumber
        )
        return announcement
    } else {
        return emptyList()
    }
}

suspend fun getAnnouncementsByAddress(
    city: String,
    street: String,
    houseNumber: Int
): List<AnnouncementModel> {
    val announcements = collection.find().toList().asReversed()
    return announcements.filter { it.isContainingAddress(city, street, houseNumber) }
}

suspend fun addAnnouncement(
    announcement: AnnouncementModel
): AnnouncementModel? {
    val announcementToInsert = announcement.copy(
        id = ObjectId().toString(),
        creationDate = OffsetDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME)
    )
    val status = collection.insertOne(announcementToInsert).wasAcknowledged()
    return if (status) announcementToInsert else null
}

suspend fun deleteAnnouncement(
    announcementId: String
): Boolean {
    val filter = Filters.eq("_id", announcementId)
    val deletedAnnouncement = collection.findOneAndDelete(filter)
    return deletedAnnouncement != null
}