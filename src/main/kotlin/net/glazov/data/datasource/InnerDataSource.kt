package net.glazov.data.datasource

import net.glazov.data.model.TariffModel
import net.glazov.data.model.posts.InnerPostModel

interface InnerDataSource {

    suspend fun getAllInnerPosts(): List<InnerPostModel>

    suspend fun getAllInnerTariffs(includeOrganizationTariffs: Boolean): List<TariffModel>

    suspend fun getActiveInnerTariffs(includeOrganizationTariffs: Boolean): List<TariffModel>

    suspend fun getArchiveInnerTariffs(includeOrganizationTariffs: Boolean): List<TariffModel>

}