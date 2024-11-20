package dev.ridill.rivo.schedules.domain.repository

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.model.ScheduleRepetition
import java.time.LocalDateTime

interface SchedulesRepository {
    suspend fun getScheduleById(id: Long): Schedule?
    fun calculateNextPaymentTimestampFromDate(
        dateTime: LocalDateTime,
        repetition: ScheduleRepetition
    ): LocalDateTime?

    fun calculateLastPaymentTimestampFromDate(
        dateTime: LocalDateTime,
        repetition: ScheduleRepetition
    ): LocalDateTime?

    suspend fun saveScheduleAndSetReminder(schedule: Schedule)
    fun scheduleReminder(schedule: Schedule)
    suspend fun createTransactionFromScheduleAndSetNextReminder(
        schedule: Schedule,
        dateTime: LocalDateTime = DateUtil.now()
    )

    suspend fun getOldestTxTimestampForSchedule(id: Long): LocalDateTime?
    suspend fun getLatestTxTimestampForSchedule(id: Long): LocalDateTime?
    suspend fun deleteScheduleById(id: Long)
    suspend fun cancelSchedule(schedule: Schedule)
    suspend fun setAllFutureScheduleReminders()
    suspend fun deleteSchedulesByIds(ids: Set<Long>)
    suspend fun updateSchedules(vararg schedule: Schedule)
}