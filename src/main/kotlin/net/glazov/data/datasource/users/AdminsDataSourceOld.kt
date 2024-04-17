package net.glazov.data.datasource.users

import net.glazov.data.model.AdminModel

interface AdminsDataSourceOld {

    suspend fun addAdmin(
        adminModel: AdminModel
    ): AdminModel?

    suspend fun getAdminById(
        adminId: String
    ): AdminModel?

    suspend fun getAdminNameById(
        adminId: String,
        useShortForm: Boolean = false
    ): String

    suspend fun login(
        login: String?,
        password: String?
    ): AdminModel?

    suspend fun changeAccountPassword(
        userId: String,
        oldPassword: String,
        newPassword: String
    ): Boolean
}