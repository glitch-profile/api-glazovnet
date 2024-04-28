package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.PostsDataSource
import net.glazov.data.model.ImageModel
import net.glazov.data.model.posts.PostModel
import org.bson.types.ObjectId
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.min

class PostsDataSourceImpl(
    db: MongoDatabase
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

    override suspend fun updatePost(id: String, title: String, text: String, image: ImageModel?): Boolean {
        val lastEditDate = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond()
        val filter = Filters.eq("_id", id)
        val update = Updates.combine(
            Updates.set(PostModel::title.name, title),
            Updates.set(PostModel::text.name, text),
            Updates.set(PostModel::image.name, image),
            Updates.set(PostModel::lastEditDate.name, lastEditDate)
        )
        val status = posts.updateOne(filter, update)
        return status.modifiedCount != 0L
    }

    override suspend fun addNewPost(title: String, text: String, image: ImageModel?): PostModel? {
        val post = PostModel(
            id = ObjectId.get().toString(),
            title = title,
            text = text,
            creationDate = OffsetDateTime.now(ZoneId.systemDefault()).toEpochSecond(),
            lastEditDate = null,
            image = image
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