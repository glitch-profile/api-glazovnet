package net.glazov.data.datasource

import net.glazov.data.model.posts.InnerPostModel

interface InnerPostsDataSource {

    suspend fun getAllPosts(): List<InnerPostModel>

    suspend fun getInnerPostById(id: String): InnerPostModel?

    suspend fun addNewPost(
        postTitle: String?,
        postText: String
    ): InnerPostModel?

}