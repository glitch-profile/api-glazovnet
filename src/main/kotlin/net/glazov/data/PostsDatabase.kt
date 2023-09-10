package net.glazov.data

import net.glazov.data.model.PostModel
import org.litote.kmongo.*

private val client = KMongo.createClient()
private val database = client.getDatabase("PostsDatabase")

private val posts = database.getCollection<PostModel>()

suspend fun getPostsList(
    limit: String?,
    startIndex: String?
): List<PostModel?> {
    val _limit = limit?.toIntOrNull() ?: 100
    val _startIndex = startIndex?.toIntOrNull() ?: 0
    val allPosts = posts.find().toList()
    val postsCount = allPosts.size
    return if (_limit >= postsCount) {
        emptyList()
    } else {
        allPosts.drop(_startIndex).take(_limit)
    }
}

suspend fun getPostById(
    id: String
): PostModel? {
    return posts.findOneById(id)
}

suspend fun updatePostById(
    newPost: PostModel
): Boolean {
    return posts.updateOneById(newPost.id, newPost).wasAcknowledged()
}

suspend fun addNewPost(
    newPost: PostModel
): Boolean {
    return posts.insertOne(newPost).wasAcknowledged()
}

suspend fun deletePostById(
    id: String
): Boolean {
    val post = posts.findOneById(id)
    return post?.let { foundPost ->
        posts.deleteOne(foundPost.id).wasAcknowledged()
    } ?: false
}