package net.glazov.data.datasource.users

import net.glazov.data.model.users.EmployeeModel
import net.glazov.data.utils.employeesroles.EmployeeRoles

interface EmployeesDataSource {

    suspend fun getEmployeeById(employeeId: String): EmployeeModel?

    suspend fun getEmployeeByPersonId(personId: String): EmployeeModel?

    suspend fun addEmployee(
        associatedPersonId: String,
        roles: List<EmployeeRoles>
    ): EmployeeModel?

    suspend fun checkEmployeeRole(employeeId: String, roleToCheck: EmployeeRoles): Boolean

    suspend fun updateRoles(employeeId: String, newRolesList: List<String>): Boolean

    suspend fun addRating(employeeId: String, rating: Double): Boolean

}