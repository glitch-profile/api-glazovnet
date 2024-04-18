package net.glazov.data.model.auth

import kotlinx.serialization.Serializable
import net.glazov.data.utils.employeesroles.EmployeeRoles

@Serializable
data class AuthResponseModel(
    val token: String,
    val personId: String,
    val clientId: String?,
    val employeeId: String?,
    val employeeRoles: List<String>?
)