package net.glazov.data.datasource.users

import net.glazov.data.model.users.PersonModel
import net.glazov.data.utils.notificationsmanager.NotificationsTopicsCodes

interface PersonsDataSource {

    suspend fun login(
        login: String,
        password: String
    ): PersonModel?

    suspend fun addPerson(
        firstName: String,
        lastName: String,
        middleName: String,
        login: String,
        password: String
    ): PersonModel?

    suspend fun getAllPersons(): List<PersonModel>

    suspend fun getPersonById(
        personId: String
    ): PersonModel?

    suspend fun getNameById(
        personId: String,
        useShortForm: Boolean = false
    ): String

    suspend fun addFcmToken(
        personId: String,
        newToken: String
    ): Boolean

    suspend fun removeFcmToken(
        personId: String,
        tokenToRemove: String
    ): Boolean

    suspend fun updateNotificationTopics(
        personId: String,
        newTopicsList: List<String>
    ): Boolean

    suspend fun updateNotificationsStatus(
        personId: String,
        newStatus: Boolean
    ): Boolean

    suspend fun getPersonTokensWithSelectedTopic(
        topic: NotificationsTopicsCodes
    ): List<List<String>>

    suspend fun changeAccountPassword(
        personId: String,
        oldPassword: String,
        newPassword: String
    ): Boolean

}