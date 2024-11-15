package dev.ridill.rivo.dashboard.data.repository

import androidx.paging.PagingData
import dev.ridill.rivo.account.domain.model.AuthState
import dev.ridill.rivo.account.domain.repository.AuthRepository
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import dev.ridill.rivo.schedules.data.toActiveSchedule
import dev.ridill.rivo.schedules.domain.model.ActiveSchedule
import dev.ridill.rivo.settings.domain.repositoty.BudgetPreferenceRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import java.time.temporal.TemporalAdjusters
import kotlin.math.absoluteValue

class DashboardRepositoryImpl(
    private val authRepo: AuthRepository,
    private val budgetRepo: BudgetPreferenceRepository,
    private val transactionDao: TransactionDao,
    private val transactionRepo: TransactionRepository,
    private val schedulesDao: SchedulesDao
) : DashboardRepository {

    private val _currentDate = MutableStateFlow(DateUtil.dateNow())
    private val currentDate = _currentDate.asStateFlow()

    override fun refreshCurrentDate() {
        _currentDate.update { DateUtil.dateNow() }
    }

    override fun getUsername(): Flow<String?> = authRepo.getAuthState().mapLatest { state ->
        when (state) {
            is AuthState.Authenticated -> state.account.displayName
            AuthState.UnAuthenticated -> null
        }
    }.distinctUntilChanged()

    override fun getCurrentBudget(): Flow<Long> = currentDate
        .flatMapLatest {
            budgetRepo.getBudgetPreferenceForMonth(it)
        }.distinctUntilChanged()

    override fun getTotalDebitsForCurrentMonth(): Flow<Double> = currentDate
        .flatMapLatest {
            transactionDao.getAmountAggregate(
                startDate = it.withDayOfMonth(1),
                endDate = it.with(TemporalAdjusters.lastDayOfMonth()),
                type = TransactionType.DEBIT,
                tagIds = null,
                showExcluded = false,
                selectedTxIds = null
            )
        }
        .map { it.absoluteValue }
        .distinctUntilChanged()

    override fun getTotalCreditsForCurrentMonth(): Flow<Double> = currentDate
        .flatMapLatest {
            transactionDao.getAmountAggregate(
                startDate = it.withDayOfMonth(1),
                endDate = it.with(TemporalAdjusters.lastDayOfMonth()),
                type = TransactionType.CREDIT,
                tagIds = null,
                showExcluded = false,
                selectedTxIds = null
            )
        }
        .map { it.absoluteValue }
        .distinctUntilChanged()

    override fun getSchedulesActiveThisMonth(): Flow<List<ActiveSchedule>> = currentDate
        .flatMapLatest { schedulesDao.getSchedulesForMonth(it) }
        .map { entities -> entities.map(ScheduleEntity::toActiveSchedule) }

    override fun getRecentSpends(): Flow<PagingData<TransactionListItem>> = currentDate
        .flatMapLatest {
            transactionRepo.getAllTransactionsPaged(
                dateRange = it.withDayOfMonth(1) to it.with(TemporalAdjusters.lastDayOfMonth()),
                type = TransactionType.DEBIT,
                showExcluded = false,
                tagIds = null,
                folderId = null
            )
        }
}