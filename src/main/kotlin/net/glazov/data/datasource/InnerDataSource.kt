package net.glazov.data.datasource

import net.glazov.data.model.posts.InnerPostModel

interface InnerDataSource {

    suspend fun getAllInnerPosts(): List<InnerPostModel>

}