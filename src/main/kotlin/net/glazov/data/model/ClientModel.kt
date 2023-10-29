package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Serializable
data class ClientModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val accountNumber: String, //Номер лицевого счёта
    val login: String,
    val password: String, //Пароль аккаунта
    val firstName: String,
    val lastName: String,
    val middleName: String? = null,
    val tariffId: String? = null, //ID подключенного тарифа
    val address: AddressModel,
    val balance: Double = 0.0, //Остаток средств на аккаунте
    val debitDate: String = OffsetDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE_TIME), //Дата списания средств
    val isAccountActive: Boolean = true,
    val connectedServices: List<String> = emptyList()
)
