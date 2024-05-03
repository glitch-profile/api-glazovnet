package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.InnerPostsDataSource
import net.glazov.data.model.posts.InnerPostModel

class InnerPostsDataSourceImpl(
    db: MongoDatabase
): InnerPostsDataSource {

    private val innerPosts = db.getCollection<InnerPostModel>("InnerPosts")

    override suspend fun getAllPosts(): List<InnerPostModel> {
        return innerPosts.find().toList().sortedByDescending { it.creationDate }
    }

    override suspend fun getInnerPostById(id: String): InnerPostModel? {
        val filter = Filters.eq("_id", id)
        return innerPosts.find(filter).singleOrNull()
    }

    override suspend fun addNewPost(postTitle: String?, postText: String): InnerPostModel? {
        val postToAdd = InnerPostModel(
            title = postTitle,
            text = postText
        )
        val status = innerPosts.insertOne(postToAdd)
        return if (status.insertedId != null) postToAdd
        else null
    }
}