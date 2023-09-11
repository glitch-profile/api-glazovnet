package net.glazov.data

import com.mongodb.client.model.InsertOneOptions
import net.glazov.data.model.PostModel
import net.glazov.data.response.SimplePostResponse
import org.litote.kmongo.*

private val client = KMongo.createClient()
private val database = client.getDatabase("GlazovNetDatabase")

private val posts = database.getCollection<PostModel>()

suspend fun getAllPosts(): List<PostModel?> {
    return posts.find().toList().asReversed()
}

suspend fun getPostsList(
    limit: String?,
    startIndex: String?
): List<PostModel?> {
    val _limit = limit?.toIntOrNull() ?: 20
    val _startIndex = startIndex?.toIntOrNull() ?: 0
    val allPosts = posts.find().toList().asReversed()
    return if (_startIndex >= allPosts.size) {
        emptyList()
    } else {
        return allPosts.drop(_startIndex).take(_limit)
    }
}

suspend fun getPostById(
    id: String
): PostModel? {
    return posts.findOneById(id)
}

suspend fun updatePostByRef(
    newPost: PostModel
): Boolean {
    return posts.findOneById(newPost.id)?.let { post ->
        posts.updateOneById(id = post.id, update = newPost).wasAcknowledged()
    } ?: false
}

suspend fun addNewPost(
    newPost: PostModel
): Boolean {
    return posts.insertOne(newPost).wasAcknowledged()
}

suspend fun deletePostById(
    id: String
): Boolean {
    return posts.findOneById(id)?.let { post ->
        posts.deleteOneById(id = post.id).wasAcknowledged()
    } ?: false
}
