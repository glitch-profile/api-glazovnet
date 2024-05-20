package net.glazov.data.datasource

import net.glazov.data.model.posts.InnerPostModel
import net.glazov.data.model.tariffs.TariffModel
import net.glazov.data.utils.RequestTariffsAccess

interface InnerDataSource {

    suspend fun getAllInnerPosts(): List<InnerPostModel>

    suspend fun getAllInnerTariffs(tariffsAccessLevel: RequestTariffsAccess): List<TariffModel>

    suspend fun getActiveInnerTariffs(tariffsAccessLevel: RequestTariffsAccess): List<TariffModel>

    suspend fun getArchiveInnerTariffs(tariffsAccessLevel: RequestTariffsAccess): List<TariffModel>

}