package net.glazov.di

import net.glazov.data.datasource.*
import net.glazov.data.datasourceimpl.*
import org.koin.dsl.module

val dataSourcesModule = module {

    single<AddressesDataSource> {
        AddressesDataSourceImpl(db = get())
    }
    single<ClientsDataSource> {
        ClientsDataSourceImpl(
            db = get(),
            addresses = get(),
            transactions = get(),
            transactionManager = get()
        )
    }
    single<AdminsDataSource> {
        AdminsDataSourceImpl(
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
        ChatDataSourceImpl(db = get(), clientsDataSource = get())
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
    single<InnerPostsDataSource> {
        InnerPostsDataSourceImpl(client = get())
    }

}