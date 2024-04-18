package net.glazov.data.model.users

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class PersonModel(
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
