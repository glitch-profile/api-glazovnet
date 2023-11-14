package net.glazov.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class RegisteredAddressesModel(
    @BsonId
    @Transient //This field will not be parsed in Json
    val id: String = ObjectId().toString(),
    val city: String,
    val street: String,
    val houseNumbers: List<String>
) {
    fun doesMatchFilter(
        city: String,
        street: String,
        isHardSearch: Boolean = false
    ): Boolean {
        return if (isHardSearch) (this.city == city && this.street == street)
        else (this.city.contains(city) && this.street.contains(street))
    }


}
