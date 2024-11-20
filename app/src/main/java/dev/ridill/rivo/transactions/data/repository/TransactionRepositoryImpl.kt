package dev.ridill.rivo.transactions.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.room.withTransaction
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.model.BasicError
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.core.domain.util.rethrowIfCoroutineCancellation
import dev.ridill.rivo.core.domain.util.logE
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView
import dev.ridill.rivo.transactions.data.toTransaction
import dev.ridill.rivo.transactions.data.toTransactionListItem
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class TransactionRepositoryImpl(
    private val db: RivoDatabase,
    private val transactionDao: TransactionDao,
    private val schedulesRepo: SchedulesRepository
) : TransactionRepository {
    override fun getAllTransactionsPaged(
        query: String?,
        dateRange: Pair<LocalDate, LocalDate>?,
        type: TransactionType?,
        showExcluded: Boolean,
        tagIds: Set<Long>?,
        folderId: Long?
    ): Flow<PagingData<TransactionListItem>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = {
            transactionDao.getTransactionsPaged(
                query = query,
                startDate = dateRange?.first?.atStartOfDay(),
                endDate = dateRange?.second?.plusDays(1L)?.atStartOfDay(),
                type = type,
                showExcluded = showExcluded,
                tagIds = tagIds?.takeIf { it.isNotEmpty() },
                folderId = folderId
            )
        }
    ).flow
        .map { it.map(TransactionDetailsView::toTransactionListItem) }

    override fun getDateSeparatedTransactions(
        query: String?,
        dateRange: Pair<LocalDate, LocalDate>?,
        type: TransactionType?,
        showExcluded: Boolean,
        tagIds: Set<Long>?,
        folderId: Long?
    ): Flow<PagingData<TransactionListItemUIModel>> = getAllTransactionsPaged(
        query = query,
        dateRange = dateRange,
        type = type,
        showExcluded = showExcluded,
        tagIds = tagIds,
        folderId = folderId
    ).map { pagingData ->
        pagingData.map { TransactionListItemUIModel.TransactionItem(it) }
    }.map { pagingData ->
        pagingData
            .insertSeparators<TransactionListItemUIModel.TransactionItem, TransactionListItemUIModel>
            { before, after ->
                if (before?.timestamp
                        ?.withDayOfMonth(1)
                        ?.toLocalDate()
                    != after?.timestamp
                        ?.withDayOfMonth(1)
                        ?.toLocalDate()
                ) after?.timestamp
                    ?.withDayOfMonth(1)
                    ?.toLocalDate()
                    ?.let { localDate ->
                        TransactionListItemUIModel.DateSeparator(localDate)
                    } else null
            }
    }

    override suspend fun saveTransaction(
        amount: Double,
        id: Long,
        note: String?,
        timestamp: LocalDateTime,
        type: TransactionType,
        tagId: Long?,
        folderId: Long?,
        scheduleId: Long?,
        excluded: Boolean
    ): Transaction = withContext(Dispatchers.IO) {
        val entity = TransactionEntity(
            id = id,
            note = note.orEmpty(),
            amount = amount,
            timestamp = timestamp,
            type = type,
            isExcluded = excluded,
            tagId = tagId,
            folderId = folderId,
            scheduleId = scheduleId
        )
        val insertedId = transactionDao.upsert(entity).first()
        entity.copy(id = insertedId)
            .toTransaction()
    }

    override suspend fun deleteSafely(
        id: Long
    ): Result<Unit, BasicError> = withContext(Dispatchers.IO) {
        try {
            db.withTransaction {
                val transaction = transactionDao.getTransactionById(id)
                    ?: throw TransactionNotFoundThrowable()
                transactionDao.delete(transaction)

                // If schedule ID is null, return out with Success
                val scheduleId = transaction.scheduleId
                    ?: return@withTransaction Result.Success(Unit)

                // Update last transaction date for schedule
                val schedule = schedulesRepo.getScheduleById(scheduleId)
                    ?: return@withTransaction Result.Success(Unit)

                val newLastPaymentTimestamp = schedulesRepo
                    .getLatestTxTimestampForSchedule(scheduleId)

                val newNextPaymentTimestamp = if (newLastPaymentTimestamp != null)
                    schedulesRepo.calculateNextPaymentTimestampFromDate(
                        newLastPaymentTimestamp,
                        schedule.repetition
                    )
                else schedule.lastPaymentTimestamp

                // update schedule and set new reminder for next date
                val updatedSchedule = schedule.copy(
                    lastPaymentTimestamp = newLastPaymentTimestamp,
                    nextPaymentTimestamp = newNextPaymentTimestamp
                )

                schedulesRepo.updateSchedules(updatedSchedule)
                Result.Success(Unit)
            }
        } catch (t: Throwable) {
            t.rethrowIfCoroutineCancellation()
            logE(t, "deleteSafely")
            Result.Error(
                error = BasicError.UNKNOWN,
                message = UiText.StringResource(resId = R.string.error_unknown, isErrorText = true)
            )
        }
    }

    override suspend fun deleteSafely(
        ids: Set<Long>
    ): Result<Unit, BasicError> = withContext(Dispatchers.IO) {
        try {
            db.withTransaction {
                val transactions = transactionDao.getTransactionsByIds(ids)
                    .ifEmpty { throw TransactionNotFoundThrowable() }
                transactionDao.deleteMultipleTransactionsById(ids)
                val scheduleIds = transactions
                    .mapNotNull { it.scheduleId }
                    .toSet()

                val updatedSchedules = scheduleIds.map { scheduleId ->
                    async(Dispatchers.IO) {
                        // Update last transaction date for schedule
                        val schedule = schedulesRepo.getScheduleById(scheduleId)
                            ?: return@async null

                        val newLastPaymentTimestamp = schedulesRepo
                            .getLatestTxTimestampForSchedule(scheduleId)

                        val newNextPaymentTimestamp = if (newLastPaymentTimestamp != null)
                            schedulesRepo.calculateNextPaymentTimestampFromDate(
                                newLastPaymentTimestamp,
                                schedule.repetition
                            )
                        else schedule.lastPaymentTimestamp

                        // update schedule and set new reminder for next date
                        schedule.copy(
                            lastPaymentTimestamp = newLastPaymentTimestamp,
                            nextPaymentTimestamp = newNextPaymentTimestamp
                        )
                    }
                }.awaitAll()
                    .filterNotNull()
                schedulesRepo.updateSchedules(*updatedSchedules.toTypedArray())
                Result.Success(Unit)
            }
        } catch (t: Throwable) {
            t.rethrowIfCoroutineCancellation()
            logE(t, "deleteSafely")
            Result.Error(
                error = BasicError.UNKNOWN,
                message = UiText.StringResource(
                    resId = R.string.error_unknown,
                    isErrorText = true
                )
            )
        }
    }

    override suspend fun toggleExcluded(id: Long, excluded: Boolean) = withContext(Dispatchers.IO) {
        transactionDao.toggleExclusionByIds(setOf(id), excluded)
    }
}

class TransactionNotFoundThrowable : Throwable()