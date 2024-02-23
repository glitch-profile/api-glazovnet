package net.glazov.data.datasource

import net.glazov.data.model.TariffModel

interface TariffsDataSource {

    suspend fun getAllTariffs(): List<TariffModel>

    suspend fun getTariffById(tariffId: String): TariffModel?

    suspend fun addTariff(newTariff: TariffModel): TariffModel?

    suspend fun deleteTariff(tariffId: String): Boolean

    suspend fun updateTariff(newTariff: TariffModel): Boolean

}