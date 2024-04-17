package net.glazov.data.model.users

import net.glazov.data.model.AddressModel
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId

data class ClientModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val personId: String,
    val accountNumber: String, //Номер лицевого счёта
    val address: AddressModel,
    val tariffId: String? = null, //ID подключенного тарифа
    val balance: Double = 0.0, //Остаток средств на аккаунте
    val accountCreationDate: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond(),
    val debitDate: Long = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond(), //Дата списания средств
    val isAccountActive: Boolean = true,
    val connectedServices: List<String> = emptyList()
)
