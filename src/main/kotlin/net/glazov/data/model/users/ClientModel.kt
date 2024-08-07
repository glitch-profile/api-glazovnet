package net.glazov.data.model.users

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.glazov.data.model.AddressModel
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId

@Serializable
data class ClientModel(
    @Transient
    @BsonId
    val id: String = ObjectId().toString(),
    val personId: String,
    val accountNumber: String, //Номер лицевого счёта
    val connectedOrganizationName: String? = null, // Название связанной организации
    val address: AddressModel,
    val tariffId: String, //ID подключенного тарифа
    val pendingTariffId: String? = null,
    val balance: Float = 0.0f, //Остаток средств на аккаунте
    val minRequiredBalance: Int,
    val accountCreationDate: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond(),
    val debitDate: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond(), //Дата списания средств
    val isAccountActive: Boolean = true,
    val accountLockTimestamp: Long? = null, //Дата, когда был заблокирован аккаунт
    val connectedServices: List<String> = emptyList()
)
