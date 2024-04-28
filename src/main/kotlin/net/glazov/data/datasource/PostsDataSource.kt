package net.glazov.data.datasource

import net.glazov.data.model.ImageModel
import net.glazov.data.model.posts.PostModel

interface PostsDataSource {

    suspend fun getAllPosts(): List<PostModel>

    suspend fun getPostsList(
        limit: String? = null,
        offset: String? = null
    ): List<PostModel>

    suspend fun getPostById(
        postId: String
    ): PostModel?

    suspend fun updatePost(
        id: String,
        title: String,
        text: String,
        image: ImageModel?
    ): Boolean

    suspend fun addNewPost(
        title: String,
        text: String,
        image: ImageModel?
    ): PostModel?

    suspend fun deletePost(
        postId: String
    ): Boolean

}