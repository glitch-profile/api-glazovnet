package net.glazov.data.model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class PostModel(
    @BsonId
    val id: String = ObjectId().toString(),
    val title: String,
    val creationDate: String,
    val shortDescription: String,
    val fullDescription: String,
    val postType: String,
    val imageUri: String?
    )
