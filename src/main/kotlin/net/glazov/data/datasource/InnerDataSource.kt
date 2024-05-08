package net.glazov.data.datasource

import net.glazov.data.model.posts.InnerPostModel
import net.glazov.data.model.tariffs.TariffModel

interface InnerDataSource {

    suspend fun getAllInnerPosts(): List<InnerPostModel>

    suspend fun getAllInnerTariffs(includeOrganizationTariffs: Boolean): List<TariffModel>

    suspend fun getActiveInnerTariffs(includeOrganizationTariffs: Boolean): List<TariffModel>

    suspend fun getArchiveInnerTariffs(includeOrganizationTariffs: Boolean): List<TariffModel>

}