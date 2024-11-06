package dev.ridill.rivo.transactions.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import dev.ridill.rivo.core.domain.util.UtilConstants
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class TransactionRepositoryImpl(
    private val dao: TransactionDao
) : TransactionRepository {
    override fun getAllTransactionsPaged(
        query: String,
        dateRange: Pair<LocalDate, LocalDate>?,
        type: TransactionType?,
        showExcluded: Boolean,
        tagIds: Set<Long>?,
        folderId: Long?
    ): Flow<PagingData<TransactionListItem>> = Pager(
        config = PagingConfig(pageSize = UtilConstants.DEFAULT_PAGE_SIZE),
        pagingSourceFactory = {
            dao.getTransactionsPaged(
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
        query: String,
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
        val insertedId = dao.upsert(entity).first()
        entity.copy(id = insertedId)
            .toTransaction()
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    override suspend fun toggleExcluded(id: Long, excluded: Boolean) = withContext(Dispatchers.IO) {
        dao.toggleExclusionByIds(setOf(id), excluded)
    }
}