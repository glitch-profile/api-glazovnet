package net.glazov.rawdata.mappers

import net.glazov.data.model.tariffs.TariffModel
import net.glazov.rawdata.dto.InnerTariffDto

fun InnerTariffDto.toTariffModel(): TariffModel {
    return TariffModel(
        id = this.id,
        name = this.name,
        description = null,
        maxSpeed = this.speed.toInt(), // kilobits/s
        costPerMonth = this.price.toInt(),
        prepaidTraffic = if (this.trafficLimit == 0L) null else this.trafficLimit / 1024, //converting to kilobytes
        prepaidTrafficDescription = null,
        isActive = this.active == "yes",
        isForOrganization = this.forOrg == "yes"
    )
}