package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.ServicesDataSource
import net.glazov.data.model.ServiceModel

class ServicesDataSourceImpl(
    db: MongoDatabase
): ServicesDataSource {

    private val services = db.getCollection<ServiceModel>("Services")

    override suspend fun getAllServices(): List<ServiceModel> {
        return services.find().toList().sortedBy { it.name }
    }

    override suspend fun getServiceById(serviceId: String): ServiceModel? {
        val filter = Filters.eq("_id", serviceId)
        return services.find(filter).singleOrNull()
    }

    override suspend fun addService(
        name: String,
        description: String,
        nameEn: String,
        descriptionEn: String,
        costPerMonth: Float,
        isActive: Boolean
    ): ServiceModel? {
        val serviceToAdd = ServiceModel(
            name = name,
            nameEn = nameEn,
            description = description,
            descriptionEn = descriptionEn,
            costPerMonth = costPerMonth,
            isActive = isActive
        )
        val result = services.insertOne(serviceToAdd)
        return if (result.insertedId != null) serviceToAdd
        else null
    }
}