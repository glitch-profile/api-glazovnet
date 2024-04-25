package net.glazov.data.model.users

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class PersonModel(
    @Transient
    @BsonId
    val id: String = ObjectId().toString(),
    val firstName: String,
    val lastName: String,
    val middleName: String,
    val login: String,
    val password: String,
    val profileAvatar: String? = null, //Аватар профиля
    val isNotificationsEnabled: Boolean = false,
    val fcmTokensList: List<String> = emptyList(), //Токен уведомлений
    val selectedNotificationsTopics: List<String> = emptyList()
)
