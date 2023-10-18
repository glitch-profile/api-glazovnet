package net.glazov.database

import com.mongodb.client.model.Filters
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoClient
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.PostModel
import org.bson.types.ObjectId

private val mongoUri = ApplicationConfig(null).tryGetString("storage.mongo_db_uri").toString()
private val client = MongoClient.create(mongoUri)
private val database = client.getDatabase("GlazovNetDatabase")
private val collection = database.getCollection<PostModel>("Posts")

suspend fun getAllPosts() = collection.find().toList().reversed()

suspend fun getPostsList(
    limit: String? = null,
    offset: String? = null
): List<PostModel> {
    val _limit = limit?.toIntOrNull() ?: 20
    val _offset = offset?.toIntOrNull() ?: 0
    val allPosts = collection.find().toList().reversed().toList()
    return if (_offset >= allPosts.size) emptyList()
    else allPosts.subList(fromIndex = _offset, toIndex = _offset + _limit)
}

suspend fun getPostById(
    id: String
): PostModel? {
    val filter = Filters.eq("_id", id)
    return collection.find(filter).toList().firstOrNull()
}

suspend fun updatePostByRef(
    newPost: PostModel
): Boolean {
    val filter = Filters.eq("_id", newPost.id)
    val post = collection.findOneAndReplace(filter, newPost)
    return post != null
}

suspend fun addNewPost(
    newPost: PostModel
): Boolean {
    return collection.insertOne(
        newPost.copy(
            id = ObjectId.get().toString()
        )
    ).wasAcknowledged()
}

suspend fun deletePostById(
    postId: String
): Boolean {
    val filter = Filters.eq("_id", postId)
    val post = collection.findOneAndDelete(filter)
    return post != null
}