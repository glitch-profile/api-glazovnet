package net.glazov.data.datasource

import net.glazov.data.model.ServiceModel

interface ServicesDataSource {

    suspend fun getAllServices(): List<ServiceModel>

    suspend fun getServiceById(serviceId: String): ServiceModel?

    suspend fun getMultipleServicesById(servicesId: List<String>): List<ServiceModel>

    suspend fun addService(
        name: String,
        description: String,
        nameEn: String,
        descriptionEn: String,
        costPerMonth: Int,
        connectionCost: Int? = null,
        isActive: Boolean = true
    ): ServiceModel?

}