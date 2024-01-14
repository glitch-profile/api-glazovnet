package net.glazov.data.model

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class PostModel(
    @BsonId
    var id: String? = ObjectId().toString(),
    val title: String,
    val creationDate: String,
    val text: String,
    val image: ImageModel? = null
    )
