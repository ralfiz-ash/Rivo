package dev.ridill.rivo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.dashboard.data.repository.DashboardRepositoryImpl
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.dashboard.presentation.DashboardViewModel
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.settings.domain.repositoty.BudgetPreferenceRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository

@Module
@InstallIn(ViewModelComponent::class)
object DashboardModule {
    @Provides
    fun provideDashboardRepository(
        authRepo: AuthRepository,
        budgetRepo: BudgetPreferenceRepository,
        transactionDao: TransactionDao,
        transactionRepo: TransactionRepository,
        schedulesDao: SchedulesDao
    ): DashboardRepository = DashboardRepositoryImpl(
        authRepo = authRepo,
        budgetRepo = budgetRepo,
        transactionDao = transactionDao,
        transactionRepo = transactionRepo,
        schedulesDao = schedulesDao
    )

    @Provides
    fun provideDashboardEventBus(): EventBus<DashboardViewModel.DashboardEvent> = EventBus()
}