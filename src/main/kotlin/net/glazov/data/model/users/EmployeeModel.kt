package net.glazov.data.model.users

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId

@Serializable
data class EmployeeModel(
    @Transient
    @BsonId
    val id: String = ObjectId().toString(),
    val personId: String,
    val accountCreationDate: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond(),
    val roles: List<String>,
    val overallRating: Int = 0, // sum of all ratings (1..5). Divide by ratingsCount to get average
    val numberOfRatings: Int = 0 // number of ratings from customers
)
