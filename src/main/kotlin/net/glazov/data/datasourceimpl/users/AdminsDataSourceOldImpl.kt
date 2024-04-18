package net.glazov.data.datasourceimpl.users

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.users.AdminsDataSourceOld
import net.glazov.data.model.AdminModel
import org.bson.types.ObjectId
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AdminsDataSourceOldImpl(
    private val db: MongoDatabase
): AdminsDataSourceOld {

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

    override suspend fun changeAccountPassword(userId: String, oldPassword: String, newPassword: String): Boolean {
        val filter = Filters.and(
            Filters.eq("_id", userId),
            Filters.eq(AdminModel::password.name, oldPassword)
        )
        val update = Updates.set(AdminModel::password.name, newPassword)
        val result = admins.updateOne(filter,update)
        return result.upsertedId != null
    }
}