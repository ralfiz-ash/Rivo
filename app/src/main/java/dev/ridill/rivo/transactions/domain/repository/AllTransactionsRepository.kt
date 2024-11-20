package dev.ridill.rivo.transactions.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.domain.model.BasicError
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Currency

interface AllTransactionsRepository {
    fun getCurrencyPreference(date: LocalDate): Flow<Currency>
    fun getDateLimits(): Flow<Pair<LocalDate, LocalDate>>
    fun getAmountAggregate(
        dateRange: Pair<LocalDate, LocalDate>? = null,
        type: TransactionType?,
        addExcluded: Boolean,
        tagIds: Set<Long>,
        selectedTxIds: Set<Long>
    ): Flow<Double>

    fun getAllTransactionsPaged(
        dateRange: Pair<LocalDate, LocalDate>? = null,
        transactionType: TransactionType? = null,
        showExcluded: Boolean = true,
        tagIds: Set<Long>? = null,
        folderId: Long? = null
    ): Flow<PagingData<TransactionListItemUIModel>>

    fun getSearchResults(
        query: String?
    ): Flow<PagingData<TransactionListItem>>

    suspend fun deleteTransactionsByIds(ids: Set<Long>): Result<Unit, BasicError>
    suspend fun setTagIdToTransactions(tagId: Long?, transactionIds: Set<Long>)
    fun getShowExcludedOption(): Flow<Boolean>
    suspend fun toggleShowExcludedOption(show: Boolean)
    suspend fun toggleTransactionExclusionByIds(ids: Set<Long>, excluded: Boolean)
    suspend fun addTransactionsToFolderByIds(ids: Set<Long>, folderId: Long)
    suspend fun removeTransactionsFromFolders(ids: Set<Long>)
    suspend fun aggregateTogether(ids: Set<Long>, dateTime: LocalDateTime): Long
}