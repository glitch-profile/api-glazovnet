package net.glazov.di

import net.glazov.data.datasource.*
import net.glazov.data.datasource.users.*
import net.glazov.data.datasourceimpl.*
import net.glazov.data.datasourceimpl.users.*
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
    //TODO: Remove this when implementation of new dataSources will be completed
    single<ClientsDataSourceOld> {
        ClientsDataSourceOldImpl(
            db = get(),
            addresses = get(),
            transactions = get(),
            transactionManager = get()
        )
    }
    single<AdminsDataSourceOld> {
        AdminsDataSourceOldImpl(
            db = get()
        )
    }
    // REMOVE UPPER SECTOR
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
    single<TransactionsDataSource> {
        TransactionsDataSourceImpl(db = get())
    }
    // RAW DATA SOURCES
    single<InnerDataSource> {
        InnerDataSourceImpl(client = get())
    }

}