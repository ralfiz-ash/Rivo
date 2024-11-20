package dev.ridill.rivo.transactions.domain.repository

import dev.ridill.rivo.core.domain.model.BasicError
import dev.ridill.rivo.core.domain.model.Result
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.model.ScheduleRepetition
import dev.ridill.rivo.transactions.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface AddEditTransactionRepository {
    suspend fun getTransactionById(id: Long): Transaction?
    fun getAmountRecommendations(): Flow<List<Long>>
    suspend fun saveTransaction(transaction: Transaction): Long
    suspend fun deleteTransaction(id: Long): Result<Unit, BasicError>
    suspend fun toggleExclusionById(id: Long, excluded: Boolean)
    suspend fun getScheduleById(id: Long): Schedule?
    suspend fun deleteSchedule(id: Long)
    suspend fun saveAsSchedule(transaction: Transaction, repetition: ScheduleRepetition)
    fun getFolderNameForId(folderId: Long?): Flow<String?>
}