package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.settings.data.local.CurrencyListDao
import dev.ridill.rivo.settings.data.local.CurrencyPreferenceDao
import dev.ridill.rivo.settings.data.repository.CurrencyRepositoryImpl
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository

@Module
@InstallIn(SingletonComponent::class)
object CurrencyModule {

    @Provides
    fun provideCurrencyDao(database: RivoDatabase): CurrencyListDao =
        database.currencyListDao()

    @Provides
    fun provideCurrencyPreferenceDao(database: RivoDatabase): CurrencyPreferenceDao =
        database.currencyPreferenceDao()

    @Provides
    fun provideCurrencyPreferenceRepository(
        dao: CurrencyPreferenceDao,
        currencyListDao: CurrencyListDao
    ): CurrencyRepository = CurrencyRepositoryImpl(
        dao = dao,
        currencyListDao = currencyListDao
    )
}