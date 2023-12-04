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

    override suspend fun getAllTariffs(): List<TariffModel> {
        return tariffs.find().toList().asReversed()
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