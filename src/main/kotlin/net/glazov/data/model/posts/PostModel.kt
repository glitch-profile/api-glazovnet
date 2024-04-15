package net.glazov.data.model.posts

import kotlinx.serialization.Serializable
import net.glazov.data.model.ImageModel
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class PostModel(
    @BsonId
    var id: String = ObjectId().toString(),
    val title: String,
    val creationDate: Long,
    val lastEditDate: Long? = null,
    val text: String,
    val image: ImageModel? = null
    )
