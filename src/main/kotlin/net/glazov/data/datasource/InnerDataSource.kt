package net.glazov.data.datasource

import net.glazov.data.model.TariffModel
import net.glazov.data.model.posts.InnerPostModel

interface InnerDataSource {

    suspend fun getAllInnerPosts(): List<InnerPostModel>

    suspend fun getInnerTariffs(includeOrgTariffs: Boolean, showOnlyActive: Boolean): List<TariffModel>

}