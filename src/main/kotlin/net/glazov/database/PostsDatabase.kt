package net.glazov.database

import com.mongodb.client.model.Filters
import net.glazov.data.model.PostModel
import org.litote.kmongo.*

private val client = KMongo.createClient()
private val database = client.getDatabase("GlazovNetDatabase")

private val collection = database.getCollection<PostModel>("Posts")

suspend fun getAllPosts(): List<PostModel?> {
    return collection.find().toList().asReversed()
}

suspend fun getPostsList(
    limit: String?,
    startIndex: String?
): List<PostModel?> {
    val _limit = limit?.toIntOrNull() ?: 20
    val _startIndex = startIndex?.toIntOrNull() ?: 0
    val allPosts = collection.find().toList().asReversed()
    return if (_startIndex >= allPosts.size) {
        emptyList()
    } else {
        return allPosts.drop(_startIndex).take(_limit)
    }
}

suspend fun getPostById(
    id: String
): PostModel? {
    return collection.findOneById(id)
}

suspend fun updatePostByRef(
    newPost: PostModel
): Boolean {
    return collection.findOneById(newPost.id)?.let { post ->
        collection.updateOneById(id = post.id, update = newPost).wasAcknowledged()
    } ?: false
}

suspend fun addNewPost(
    newPost: PostModel
): Boolean {
    val post = newPost.copy(
        id = newId<String>().toString()
    )
    return collection.insertOne(post).wasAcknowledged()
}

suspend fun deletePostById(
    id: String
): Boolean {
    val filter = Filters.eq("_id", id)
    val post = collection.findOneAndDelete(filter)
    return post !== null
}