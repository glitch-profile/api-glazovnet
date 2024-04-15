package net.glazov.data.datasource

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
        newPost: PostModel
    ): Boolean

    suspend fun addNewPost(
        newPost: PostModel
    ): PostModel?

    suspend fun deletePost(
        postId: String
    ): Boolean

}