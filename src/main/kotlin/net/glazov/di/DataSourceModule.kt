package net.glazov.di

import net.glazov.data.datasource.*
import net.glazov.data.datasource.users.ClientsDataSource
import net.glazov.data.datasource.users.EmployeesDataSource
import net.glazov.data.datasource.users.PersonsDataSource
import net.glazov.data.datasourceimpl.*
import net.glazov.data.datasourceimpl.users.ClientsDataSourceImpl
import net.glazov.data.datasourceimpl.users.EmployeesDataSourceImpl
import net.glazov.data.datasourceimpl.users.PersonsDataSourceImpl
import org.koin.dsl.module

val dataSourcesModule = module {

    single<AddressesDataSource> {
        AddressesDataSourceImpl(db = get())
    }
    single<PersonsDataSource> {
        PersonsDataSourceImpl(get())
    }
    single<ClientsDataSource> {
        ClientsDataSourceImpl(
            db = get(),
            persons = get(),
            addresses = get(),
            transactions = get(),
            transactionManager = get()
        )
    }
    single<EmployeesDataSource> {
        EmployeesDataSourceImpl(get())
    }
    single<AnnouncementsDataSource> {
        AnnouncementsDataSourceImpl(
            db = get(),
            clients = get()
        )
    }
    single<ChatDataSource> {
        ChatDataSourceImpl(db = get(), clients = get(), persons = get())
    }
    single<TariffsDataSource> {
        TariffsDataSourceImpl(db = get())
    }
    single<PostsDataSource> {
        PostsDataSourceImpl(db = get())
    }
    single<InnerPostsDataSource> {
        InnerPostsDataSourceImpl(db = get())
    }
    single<TransactionsDataSource> {
        TransactionsDataSourceImpl(db = get())
    }
    single<ServicesDataSource> {
        ServicesDataSourceImpl(db = get())
    }
    // RAW DATA SOURCES
    single<InnerDataSource> {
        InnerDataSourceImpl(client = get())
    }

}