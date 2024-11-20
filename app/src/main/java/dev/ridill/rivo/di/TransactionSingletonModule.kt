package dev.ridill.rivo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyPreferenceRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.repository.TransactionRepositoryImpl
import dev.ridill.rivo.transactions.domain.autoDetection.RegexTransactionDataExtractor
import dev.ridill.rivo.transactions.domain.autoDetection.TransactionAutoDetectService
import dev.ridill.rivo.transactions.domain.autoDetection.TransactionDataExtractor
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.notification.TransactionAutoDetectNotificationHelper
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object TransactionSingletonModule {
    @Provides
    fun provideTransactionDao(db: RivoDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideTransactionRepository(
        db: RivoDatabase,
        transactionDao: TransactionDao,
        schedulesRepo: SchedulesRepository
    ): TransactionRepository = TransactionRepositoryImpl(
        db = db,
        transactionDao = transactionDao,
        schedulesRepo = schedulesRepo
    )

    @Provides
    fun provideTransactionDataExtractor(): TransactionDataExtractor =
        RegexTransactionDataExtractor()

    @Provides
    fun provideTransactionSmsService(
        extractor: TransactionDataExtractor,
        currencyPreferenceRepository: CurrencyPreferenceRepository,
        transactionRepository: TransactionRepository,
        notificationHelper: NotificationHelper<Transaction>,
        @ApplicationScope applicationScope: CoroutineScope,
    ): TransactionAutoDetectService = TransactionAutoDetectService(
        extractor = extractor,
        currencyPrefRepo = currencyPreferenceRepository,
        transactionRepo = transactionRepository,
        notificationHelper = notificationHelper,
        applicationScope = applicationScope
    )

    @Provides
    fun provideTransactionAutoDetectNotificationHelper(
        @ApplicationContext context: Context
    ): NotificationHelper<Transaction> = TransactionAutoDetectNotificationHelper(context)
}