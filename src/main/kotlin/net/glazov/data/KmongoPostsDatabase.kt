package net.glazov.data

import net.glazov.data.model.PostModel
import org.litote.kmongo.*

private val client = KMongo.createClient()
private val database = client.getDatabase("GlazovNetDatabase")

private val posts = database.getCollection<PostModel>("Posts")

suspend fun getAllPostsKMongo(): List<PostModel?> {
    return posts.find().toList().asReversed()
}
@Deprecated("Kmongo")
suspend fun getPostsListKMongo(
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
@Deprecated("Kmongo")
suspend fun getPostByIdKMongo(
    id: String
): PostModel? {
    return posts.findOneById(id)
}
@Deprecated("Kmongo")
suspend fun updatePostByRefKMongo(
    newPost: PostModel
): Boolean {
    return posts.findOneById(newPost.id)?.let { post ->
        posts.updateOneById(id = post.id, update = newPost).wasAcknowledged()
    } ?: false
}
@Deprecated("Kmongo")
suspend fun addNewPostKmongo(
    newPost: PostModel
): Boolean {
    val post = newPost.copy(
        id = newId<String>().toString()
    )
    return posts.insertOne(post).wasAcknowledged()
}
@Deprecated("Kmongo")
suspend fun deletePostByIdKmongo(
    id: String
): Boolean {
    return posts.findOneById(id)?.let { post ->
        posts.deleteOneById(id = post.id).wasAcknowledged()
    } ?: false
}