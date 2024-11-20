package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.folders.domain.repository.FolderDetailsRepository
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyPreferenceRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.repository.AddEditTransactionRepositoryImpl
import dev.ridill.rivo.transactions.data.repository.AllTransactionsRepositoryImpl
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import dev.ridill.rivo.transactions.domain.repository.AllTransactionsRepository
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import dev.ridill.rivo.transactions.presentation.addEditTransaction.AddEditTransactionViewModel
import dev.ridill.rivo.transactions.presentation.allTransactions.AllTransactionsViewModel

@Module
@InstallIn(ViewModelComponent::class)
object TransactionViewModelModule {

    @Provides
    fun provideAddEditTransactionRepository(
        dao: TransactionDao,
        repo: TransactionRepository,
        schedulesRepo: SchedulesRepository,
        folderRepo: FolderDetailsRepository
    ): AddEditTransactionRepository = AddEditTransactionRepositoryImpl(
        dao = dao,
        repo = repo,
        schedulesRepo = schedulesRepo,
        folderRepo = folderRepo
    )

    @Provides
    fun provideAddEditTransactionEventBus(): EventBus<AddEditTransactionViewModel.AddEditTransactionEvent> =
        EventBus()

    @Provides
    fun provideAllTransactionsRepository(
        db: RivoDatabase,
        dao: TransactionDao,
        transactionRepo: TransactionRepository,
        preferencesManager: PreferencesManager,
        currencyPreferenceRepository: CurrencyPreferenceRepository
    ): AllTransactionsRepository = AllTransactionsRepositoryImpl(
        db = db,
        dao = dao,
        repo = transactionRepo,
        preferencesManager = preferencesManager,
        currencyPrefRepo = currencyPreferenceRepository
    )

    @Provides
    fun provideAllTransactionEventBus(): EventBus<AllTransactionsViewModel.AllTransactionsEvent> =
        EventBus()
}