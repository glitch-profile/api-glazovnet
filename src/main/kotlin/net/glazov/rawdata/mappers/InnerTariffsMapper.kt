package net.glazov.rawdata.mappers

import net.glazov.data.model.TariffModel
import net.glazov.rawdata.dto.InnerTariffDto

fun InnerTariffDto.toTariffModel(): TariffModel {
    val categoryCode = if (this.trafficLimit == 0L && this.active == "yes") {
        0 //Unlimited
    } else if (this.active == "no") 2 else 1 // 1 - Limited, 2 - Archive
    return TariffModel(
        id = this.id,
        name = this.name,
        categoryCode = categoryCode,
        maxSpeed = this.speed.toInt(),
        costPerMonth = this.price.toInt(),
        prepaidTraffic = if (categoryCode == 0) null else this.trafficLimit,
        prepaidTrafficDescription = null,
        isForOrganization = this.forOrg != "no"
    ) //in kilobytes
}