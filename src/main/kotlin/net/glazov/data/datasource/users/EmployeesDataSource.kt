package net.glazov.data.datasource.users

import net.glazov.data.model.users.EmployeeModel
import net.glazov.data.model.users.PersonModel
import net.glazov.data.utils.EmployeeRoles

interface EmployeesDataSource {

    suspend fun getAllEmployees(): List<EmployeeModel>

    suspend fun getEmployeeById(employeeId: String): EmployeeModel?

    suspend fun getEmployeeByPersonId(personId: String): EmployeeModel?

    suspend fun getAssociatedPerson(employeeId: String): PersonModel?

    suspend fun addEmployee(
        associatedPersonId: String,
        roles: List<EmployeeRoles>
    ): EmployeeModel?

    suspend fun checkEmployeeRole(employeeId: String, roleToCheck: EmployeeRoles): Boolean

    suspend fun updateRoles(employeeId: String, newRolesList: List<String>): Boolean

    suspend fun addRating(employeeId: String, rating: Int): Boolean

}