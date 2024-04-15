package net.glazov.data.datasourceimpl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import net.glazov.data.datasource.InnerPostsDataSource
import net.glazov.data.model.posts.InnerPostModel
import net.glazov.rawdata.dto.InnerNewsDto
import net.glazov.rawdata.mappers.toInnerPostModel

private const val PATH = "api/v2/news"

class InnerPostsDataSourceImpl(
    private val client: HttpClient
): InnerPostsDataSource {

    override suspend fun getAllInnerPosts(): List<InnerPostModel> {
        val innerNews: List<InnerNewsDto> = client.get(PATH).body()
        val mappedPosts = innerNews.map { it.toInnerPostModel() }
        return mappedPosts
    }
}