package net.glazov.data.datasourceimpl

import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.AdminsDataSource
import net.glazov.data.model.AddressModel
import net.glazov.data.model.AdminModel
import net.glazov.data.model.ClientModel
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AdminsDataSourceImpl(
    private val db: MongoDatabase
): AdminsDataSource {

    private val admins = db.getCollection<AdminModel>("Admins")

    override suspend fun addAdmin(adminModel: AdminModel): AdminModel? {
        val creationDate = LocalDate.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_DATE)
        val newAdmin = adminModel.copy(
            id = ObjectId().toString(),
            accountCreationDate = creationDate
        )
        val status = admins.insertOne(newAdmin).wasAcknowledged()
        return if (status) newAdmin else null
    }

    override suspend fun getAdminById(adminId: String): AdminModel? {
        val filter = Filters.eq("_id", adminId)
        return admins.find(filter).toList().singleOrNull()
    }

    override suspend fun getAdminNameById(adminId: String, useShortForm: Boolean): String {
        val admin = getAdminById(adminId)
        return if (admin != null) {
            if (useShortForm) "${admin.firstName} ${admin.middleName}"
            else "${admin.lastName} ${admin.firstName} ${admin.middleName}"
        }
        else "Unknown administrator"
    }

    override suspend fun login(login: String?, password: String?): AdminModel? {
        val loginFilter = Filters.eq(AdminModel::login.name, login)
        val passwordFilter = Filters.eq(AdminModel::password.name, password)
        val filter = Filters.and(loginFilter, passwordFilter)
        return admins.find(filter).singleOrNull()
    }
}