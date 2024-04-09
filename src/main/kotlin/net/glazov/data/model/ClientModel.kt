package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class ClientModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val profileAvatar: String? = null, //Аватар профиля
    val accountNumber: String, //Номер лицевого счёта
    val login: String,
    val password: String, //Пароль аккаунта
    val isNotificationsEnabled: Boolean? = null,
    val fcmTokensList: List<String>? = null, //Токен уведомлений
    val selectedNotificationsTopics: List<String>? = null,
    val firstName: String,
    val lastName: String,
    val middleName: String? = null,
    val tariffId: String? = null, //ID подключенного тарифа
    val address: AddressModel,
    val balance: Double = 0.0, //Остаток средств на аккаунте
    val accountCreationDate: String = "",
    val debitDate: String = "", //Дата списания средств
    val isAccountActive: Boolean = true,
    val connectedServices: List<String> = emptyList()
)
