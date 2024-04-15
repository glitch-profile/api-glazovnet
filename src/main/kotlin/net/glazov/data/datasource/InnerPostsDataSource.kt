package net.glazov.data.datasource

import net.glazov.data.model.posts.InnerPostModel

interface InnerPostsDataSource {

    suspend fun getAllInnerPosts(): List<InnerPostModel>

}