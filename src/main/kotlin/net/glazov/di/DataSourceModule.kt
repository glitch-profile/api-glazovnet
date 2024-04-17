package net.glazov.di

import net.glazov.data.datasource.*
import net.glazov.data.datasource.users.AdminsDataSourceOld
import net.glazov.data.datasource.users.ClientsDataSourceOld
import net.glazov.data.datasourceimpl.*
import net.glazov.data.datasourceimpl.users.AdminsDataSourceOldImpl
import net.glazov.data.datasourceimpl.users.ClientsDataSourceOldImpl
import org.koin.dsl.module

val dataSourcesModule = module {

    single<AddressesDataSource> {
        AddressesDataSourceImpl(db = get())
    }
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
    single<AnnouncementsDataSource> {
        AnnouncementsDataSourceImpl(
            db = get(),
            clients = get()
        )
    }
    single<ChatDataSource> {
        ChatDataSourceImpl(db = get(), clientsDataSourceOld = get())
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