package net.glazov.data.datasourceimpl.users

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.model.users.PersonModel
import net.glazov.data.utils.notificationsmanager.NotificationsTopicsCodes

class PersonsDataSourceImpl(
    db: MongoDatabase
): PersonsDataSource {

    private val persons = db.getCollection<PersonModel>("Persons")

    override suspend fun login(login: String, password: String): PersonModel? {
        val loginFilter = Filters.eq(PersonModel::login.name, login)
        val passwordFilter = Filters.eq(PersonModel::password.name, password)
        val filter = Filters.and(loginFilter, passwordFilter)
        return persons.find(filter).singleOrNull()
    }

    override suspend fun addPerson(
        firstName: String,
        lastName: String,
        middleName: String,
        login: String,
        password: String
    ): PersonModel? {
        val person = PersonModel(
            firstName = firstName,
            lastName = lastName,
            middleName = middleName,
            login = login,
            password = password
        )
        val status = persons.insertOne(person)
        return if (status.insertedId != null) person else null
    }

    override suspend fun getPersonById(personId: String): PersonModel? {
        val filter = Filters.eq("_id", personId)
        return persons.find(filter).singleOrNull()
    }

    override suspend fun getNameById(personId: String, useShortForm: Boolean): String {
        val person = getPersonById(personId)
        return if (person != null) {
            if (useShortForm) "${person.firstName} ${person.middleName}"
            else "${person.lastName} ${person.firstName} ${person.middleName}"
        } else "Mr. Unknown"
    }

    override suspend fun addFcmToken(personId: String, newToken: String): Boolean {
        val filter = Filters.eq("_id", personId)
        val update = Updates.addToSet(PersonModel::fcmTokensList.name, newToken)
        val result = persons.updateOne(filter, update)
        return result.upsertedId != null
    }

    override suspend fun removeFcmToken(personId: String, tokenToRemove: String): Boolean {
        val filter = Filters.eq("_id", personId)
        val update = Updates.pull(PersonModel::fcmTokensList.name, tokenToRemove)
        val result = persons.updateOne(filter, update)
        return result.upsertedId != null
    }

    override suspend fun updateNotificationTopics(personId: String, newTopicsList: List<String>): Boolean {
        val filter = Filters.eq("_id", personId)
        val update = Updates.set(PersonModel::selectedNotificationsTopics.name, newTopicsList)
        return persons.updateOne(filter, update).upsertedId != null
    }

    override suspend fun updateNotificationsStatus(personId: String, newStatus: Boolean): Boolean {
        val filter = Filters.eq("_id", personId)
        val update = Updates.set(PersonModel::isNotificationsEnabled.name, newStatus)
        return persons.updateOne(filter, update).upsertedId != null
    }

    override suspend fun getPersonTokensWithSelectedTopic(topic: NotificationsTopicsCodes): List<List<String>> {
        val filter = Filters.and(
            listOf(
                Filters.eq(PersonModel::isNotificationsEnabled.name, true),
                Filters.eq(PersonModel::selectedNotificationsTopics.name, topic.name),
                Filters.ne(PersonModel::fcmTokensList.name, emptyList<String>())
            )
        )
        return persons.find(filter).toList().map {
            it.fcmTokensList
        }
    }

    override suspend fun changeAccountPassword(personId: String, oldPassword: String, newPassword: String): Boolean {
        val filter = Filters.and(
            Filters.eq("_id", personId),
            Filters.eq(PersonModel::password.name, oldPassword)
        )
        val update = Updates.set(PersonModel::password.name, newPassword)
        val result = persons.updateOne(filter,update)
        return result.upsertedId != null
    }
}