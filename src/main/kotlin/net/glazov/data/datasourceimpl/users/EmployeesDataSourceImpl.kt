package net.glazov.data.datasourceimpl.users

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.model.users.EmployeeModel
import net.glazov.data.model.users.PersonModel
import net.glazov.data.utils.employeesroles.EmployeeRoles
import kotlin.math.max
import kotlin.math.min

class EmployeesDataSourceImpl(
    db: MongoDatabase
): EmployeesDataSource {

    private val employees = db.getCollection<EmployeeModel>("Employees")

    override suspend fun getAllEmployees(): List<EmployeeModel> {
        return employees.find().toList()
    }

    override suspend fun getEmployeeById(employeeId: String): EmployeeModel? {
        val filter = Filters.eq("_id", employeeId)
        return employees.find(filter).singleOrNull()
    }

    override suspend fun getEmployeeByPersonId(personId: String): EmployeeModel? {
        val filter = Filters.eq(EmployeeModel::personId.name, personId)
        return employees.find(filter).singleOrNull()
    }

    override suspend fun addEmployee(associatedPersonId: String, roles: List<EmployeeRoles>): EmployeeModel? {
        val isPersonIsAvailable = getEmployeeByPersonId(associatedPersonId) == null
        return if (isPersonIsAvailable) {
            val employee = EmployeeModel(
                personId = associatedPersonId,
                roles = roles.map { it.name }
            )
            val status = employees.insertOne(employee)
            if (status.insertedId != null) employee else null
        } else null
    }

    override suspend fun checkEmployeeRole(employeeId: String, roleToCheck: EmployeeRoles): Boolean {
        val filter = Filters.and(
            Filters.eq("_id", employeeId),
            Filters.eq(EmployeeModel::roles.name, roleToCheck)
        )
        return employees.find(filter).singleOrNull() != null
    }

    override suspend fun updateRoles(employeeId: String, newRolesList: List<String>): Boolean {
        val filter = Filters.eq("_id", employeeId)
        val update = Updates.set(PersonModel::selectedNotificationsTopics.name, newRolesList)
        return employees.updateOne(filter, update).upsertedId != null
    }

    override suspend fun addRating(employeeId: String, rating: Int): Boolean {
        val normalizedRating = max(1, min(rating, 5))
        val filter = Filters.eq("_id", employeeId)
        val update = Updates.combine(
            Updates.inc(EmployeeModel::overallRating.name, rating),
            Updates.inc(EmployeeModel::numberOfRatings.name, 1)
        )
        val result = employees.updateOne(filter, update)
        return result.upsertedId != null
    }
}