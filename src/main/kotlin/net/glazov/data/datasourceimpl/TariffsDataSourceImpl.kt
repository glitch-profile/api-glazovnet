package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.TariffsDataSource
import net.glazov.data.model.TariffModel
import org.bson.types.ObjectId

class TariffsDataSourceImpl(
    private val db: MongoDatabase
): TariffsDataSource {

    private val tariffs = db.getCollection<TariffModel>("Tariffs")

    override suspend fun getAllTariffs(includeOrganizationTariffs: Boolean): List<TariffModel> {
        return if (includeOrganizationTariffs) {
            tariffs.find().toList().reversed()
        } else {
            val filter = Filters.eq(TariffModel::isForOrganization.name, false)
            tariffs.find(filter).toList().reversed()
        }
    }

    override suspend fun getActiveTariffs(includeOrganizationTariffs: Boolean): List<TariffModel> {
        val filter = Filters.eq(TariffModel::isActive.name, true)
        val organisationFilter = Filters.and(
            filter, Filters.eq(TariffModel::isForOrganization.name, false)
        )
        return if (includeOrganizationTariffs) {
            tariffs.find(filter).toList().reversed()
        } else tariffs.find(organisationFilter).toList().reversed()
    }

    override suspend fun getArchiveTariffs(includeOrganizationTariffs: Boolean): List<TariffModel> {
        val filter = Filters.eq(TariffModel::isActive.name, false)
        val organisationFilter = Filters.and(
            filter, Filters.eq(TariffModel::isForOrganization.name, false)
        )
        return if (includeOrganizationTariffs) {
            tariffs.find(filter).toList().reversed()
        } else tariffs.find(organisationFilter).toList().reversed()
    }

    override suspend fun getTariffById(tariffId: String): TariffModel? {
        val filter = Filters.eq("_id", tariffId)
        return tariffs.find(filter).toList().singleOrNull()
    }

    override suspend fun addTariff(newTariff: TariffModel): TariffModel? {
        val tariff = newTariff.copy(
            id = ObjectId().toString()
        )
        val status = tariffs.insertOne(tariff).wasAcknowledged()
        return if (status)
            tariff else null
    }

    override suspend fun deleteTariff(tariffId: String): Boolean {
        val filter = Filters.eq("_id", tariffId)
        val tariff = tariffs.findOneAndDelete(filter)
        return tariff != null
    }

    override suspend fun updateTariff(newTariff: TariffModel): Boolean {
        val filter = Filters.eq("_id", newTariff.id)
        val tariff = tariffs.findOneAndReplace(filter, newTariff)
        return tariff != null
    }
}