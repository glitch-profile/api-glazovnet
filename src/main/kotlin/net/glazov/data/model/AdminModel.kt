package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class AdminModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val login: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val middleName: String? = null,
    val accountCreationDate: String = "",
    val isAccountActive: Boolean = true
)
