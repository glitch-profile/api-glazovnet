package net.glazov.data.datasourceimpl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.config.*
import net.glazov.data.datasource.InnerDataSource
import net.glazov.data.model.TariffModel
import net.glazov.data.model.posts.InnerPostModel
import net.glazov.rawdata.dto.InnerNewsDto
import net.glazov.rawdata.dto.InnerTariffDto
import net.glazov.rawdata.mappers.toInnerPostModel
import net.glazov.rawdata.mappers.toTariffModel

private const val NEWS_PATH = "api/v2/news"
private const val TARIFFS_PATH = "api/v2/tariffs"

class InnerDataSourceImpl(
    private val client: HttpClient
): InnerDataSource {

    val PATH = ApplicationConfig(null).tryGetString("glazov_net_server_data.host")
    val PATH_TEST = ApplicationConfig(null).tryGetString("glazov_net_server_data.host_test")

    override suspend fun getAllInnerPosts(): List<InnerPostModel> {
        val innerNews: List<InnerNewsDto> = client.get("$PATH/$NEWS_PATH").body()
        val mappedPosts = innerNews.map { it.toInnerPostModel() }
        return mappedPosts.reversed()
    }

    override suspend fun getAllInnerTariffs(includeOrganizationTariffs: Boolean): List<TariffModel> {
        val innerTariffs: List<InnerTariffDto> = client.get("$PATH_TEST/$TARIFFS_PATH").body()
        val filteredTariffs = innerTariffs.asSequence()
            .filter { it.name.isNotEmpty() }
            .filter { if (!includeOrganizationTariffs) it.forOrg == "no" else true }
            .map { it.toTariffModel() }
            .toList()
        return filteredTariffs.reversed()
    }

    override suspend fun getActiveInnerTariffs(includeOrganizationTariffs: Boolean): List<TariffModel> {
        val innerTariffs: List<InnerTariffDto> = client.get("$PATH_TEST/$TARIFFS_PATH").body()
        val filteredTariffs = innerTariffs.asSequence()
            .filter { it.name.isNotEmpty() }
            .filter { it.active == "yes" }
            .filter { if (!includeOrganizationTariffs) it.forOrg == "no" else true }
            .map { it.toTariffModel() }
            .toList()
        return filteredTariffs.reversed()
    }

    override suspend fun getArchiveInnerTariffs(includeOrganizationTariffs: Boolean): List<TariffModel> {
        val innerTariffs: List<InnerTariffDto> = client.get("$PATH_TEST/$TARIFFS_PATH").body()
        val filteredTariffs = innerTariffs.asSequence()
            .filter { it.name.isNotEmpty() }
            .filter { it.active == "no" }
            .filter { if (!includeOrganizationTariffs) it.forOrg == "no" else true }
            .map { it.toTariffModel() }
            .toList()
        return filteredTariffs.reversed()
    }


}