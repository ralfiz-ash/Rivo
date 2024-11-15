package dev.ridill.rivo.schedules.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

@Dao
interface SchedulesDao : BaseDao<ScheduleEntity> {

    @Query(
        """
        SELECT *
        FROM schedules_table
        ORDER BY CASE
            WHEN next_reminder_timestamp IS NULL THEN 2
            WHEN strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', next_reminder_timestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', DATE('now')) THEN 0
            ELSE 1
            END ASC
    """
    )
    fun getSchedulesPaged(): PagingSource<Int, ScheduleEntity>

    @Query("SELECT * FROM schedules_table WHERE id = :id")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    @Query("SELECT * FROM schedules_table WHERE next_reminder_timestamp > :date")
    suspend fun getAllSchedulesAfterDate(date: LocalDate): List<ScheduleEntity>

    @Query("SELECT MAX(DATETIME(timestamp)) FROM transaction_table WHERE schedule_id = :id")
    suspend fun getLastTransactionTimestampForSchedule(id: Long): LocalDateTime?

    @Query(
        """
        SELECT *
        FROM schedules_table
        WHERE strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', next_reminder_timestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :date)
        ORDER BY DATETIME(next_reminder_timestamp) ASC
    """
    )
    fun getSchedulesForMonth(date: LocalDate): Flow<List<ScheduleEntity>>

    @Query("DELETE FROM schedules_table WHERE id IN (:ids)")
    suspend fun deleteSchedulesById(ids: Set<Long>)
}