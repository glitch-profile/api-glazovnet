package net.glazov.data.datasource

import net.glazov.data.model.tariffs.TariffModel
import net.glazov.data.utils.RequestTariffsAccess

interface TariffsDataSource {

    suspend fun getAllTariffs(tariffsAccessLevel: RequestTariffsAccess): List<TariffModel>

    suspend fun getActiveTariffs(tariffsAccessLevel: RequestTariffsAccess): List<TariffModel>

    suspend fun getArchiveTariffs(tariffsAccessLevel: RequestTariffsAccess): List<TariffModel>

    suspend fun getTariffById(tariffId: String): TariffModel?

    suspend fun addTariff(newTariff: TariffModel): TariffModel?

    suspend fun deleteTariff(tariffId: String): Boolean

    suspend fun updateTariff(newTariff: TariffModel): Boolean

}