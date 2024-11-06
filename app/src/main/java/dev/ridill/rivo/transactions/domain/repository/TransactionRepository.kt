package dev.ridill.rivo.transactions.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface TransactionRepository {
    fun getAllTransactionsPaged(
        query: String = String.Empty,
        dateRange: Pair<LocalDate, LocalDate>? = null,
        type: TransactionType? = null,
        showExcluded: Boolean = true,
        tagIds: Set<Long>? = null,
        folderId: Long? = null
    ): Flow<PagingData<TransactionListItem>>

    fun getDateSeparatedTransactions(
        query: String = String.Empty,
        dateRange: Pair<LocalDate, LocalDate>? = null,
        type: TransactionType? = null,
        showExcluded: Boolean = true,
        tagIds: Set<Long>? = null,
        folderId: Long? = null
    ): Flow<PagingData<TransactionListItemUIModel>>

    suspend fun saveTransaction(
        amount: Double,
        id: Long = RivoDatabase.DEFAULT_ID_LONG,
        note: String? = null,
        timestamp: LocalDateTime = DateUtil.now(),
        type: TransactionType = TransactionType.DEBIT,
        tagId: Long? = null,
        folderId: Long? = null,
        scheduleId: Long? = null,
        excluded: Boolean = false
    ): Transaction

    suspend fun delete(id: Long)
    suspend fun toggleExcluded(id: Long, excluded: Boolean)
}