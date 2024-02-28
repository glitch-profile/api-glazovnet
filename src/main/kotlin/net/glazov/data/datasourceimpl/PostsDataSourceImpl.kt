package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.PostsDataSource
import net.glazov.data.model.PostModel
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.min

class PostsDataSourceImpl(
    private val db: MongoDatabase
): PostsDataSource {

    private val posts = db.getCollection<PostModel>("Posts")

    override suspend fun getAllPosts(): List<PostModel> {
        return posts.find().toList().reversed()
    }

    override suspend fun getPostsList(limit: String?, offset: String?): List<PostModel> {
        val _limit = limit?.toIntOrNull() ?: 20
        val _offset = offset?.toIntOrNull() ?: 0
        val allPosts = posts.find().toList().reversed().toList()
        return if (_offset >= allPosts.size) emptyList()
        else allPosts.subList(fromIndex = _offset, toIndex = min((_offset + _limit), allPosts.lastIndex))
    }

    override suspend fun getPostById(postId: String): PostModel? {
        val filter = Filters.eq("_id", postId)
        return posts.find(filter).toList().firstOrNull()
    }

    override suspend fun updatePost(newPost: PostModel): Boolean {
        val editedPost = newPost.copy(
            lastEditDate = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
        )
        val filter = Filters.eq("_id", newPost.id)
        val post = posts.findOneAndReplace(filter, editedPost)
        return post != null
    }

    override suspend fun addNewPost(newPost: PostModel): PostModel? {
        val post = newPost.copy(
            id = ObjectId.get().toString(),
            creationDate = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
        )
        val status = posts.insertOne(post).wasAcknowledged()
        return if (status)
            post else null
    }

    override suspend fun deletePost(postId: String): Boolean {
        val filter = Filters.eq("_id", postId)
        val post = posts.findOneAndDelete(filter)
        return post != null
    }
}