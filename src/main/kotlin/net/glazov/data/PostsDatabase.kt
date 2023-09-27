package net.glazov.data

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoClient
import kotlinx.coroutines.flow.toList
import net.glazov.data.model.PostModel
import org.bson.types.ObjectId

//private const val MONGO_URI = "mongodb://localhost:27017"
//private const val DATABASE_NAME = "GlazovNetDatabase"
//private const val COLLECTION_POSTS = "Posts"
//
//private val mongoClient = MongoClient.create(MONGO_URI)
//private val mongoDatatbase = mongoClient.getDatabase(DATABASE_NAME)
//private val postsCollection = mongoDatatbase.getCollection<PostModel>(COLLECTION_POSTS)
//
//suspend fun getAllPosts(): List<PostModel?> {
//    return postsCollection.find().toList().asReversed()
//}
//
//suspend fun getPostsList(
//    limit: String?,
//    startIndex: String?
//): List<PostModel?> {
//    val _limit = limit?.toIntOrNull() ?: 20
//    val _startIndex = startIndex?.toIntOrNull() ?: 0
//    val posts = postsCollection.find().toList().asReversed()
//    return if (_startIndex > posts.size) {
//        emptyList()
//    } else {
//        posts.drop(_startIndex).take(_limit)
//    }
//}
//
//suspend fun getPostById(
//    id: String
//): PostModel? {
//    val filter = Filters.eq(PostModel::id.name, id)
//    val post = postsCollection.find(filter)
//    return post.toList().firstOrNull()
//}
//
//suspend fun updatePostByRef(
//    newPost: PostModel
//): Boolean {
//    val filter = Filters.eq(PostModel::id.name, newPost.id)
//    return postsCollection.replaceOne(filter, newPost).wasAcknowledged()
//}
//
//suspend fun addNewPost(
//    newPost: PostModel
//): Boolean {
//    val post = newPost.copy(
//        id = ObjectId().toString()
//    )
//    return postsCollection.insertOne(post).wasAcknowledged()
//}
//
//suspend fun deletePostById(
//    id: String
//): Boolean {
//    val filter = Filters.eq(PostModel::id.name, id)
//    return postsCollection.deleteOne(filter).wasAcknowledged()
//}
