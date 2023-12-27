package net.glazov.data.datasource

import net.glazov.data.model.AdminModel

interface AdminsDataSource {

    suspend fun addAdmin(
        adminModel: AdminModel
    ): AdminModel?

    suspend fun getAdminById(
        adminId: String
    ): AdminModel?

    suspend fun login(
        login: String?,
        password: String?
    ): AdminModel?
}