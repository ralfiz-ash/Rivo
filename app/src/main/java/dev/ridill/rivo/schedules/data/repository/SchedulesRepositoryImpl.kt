package dev.ridill.rivo.schedules.data.repository

import androidx.room.withTransaction
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.data.util.trySuspend
import dev.ridill.rivo.core.domain.service.ReceiverService
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import dev.ridill.rivo.schedules.data.toEntity
import dev.ridill.rivo.schedules.data.toSchedule
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.model.ScheduleRepetition
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.schedules.domain.scheduleReminder.ScheduleReminder
import dev.ridill.rivo.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class SchedulesRepositoryImpl(
    private val db: RivoDatabase,
    private val dao: SchedulesDao,
    private val txRepo: TransactionRepository,
    private val scheduler: ScheduleReminder,
    private val receiverService: ReceiverService
) : SchedulesRepository {
    override suspend fun getScheduleById(
        id: Long
    ): Schedule? = withContext(Dispatchers.IO) {
        dao.getScheduleById(id)?.toSchedule()
    }

    override fun getNextReminderFromDate(
        dateTime: LocalDateTime,
        repetition: ScheduleRepetition
    ): LocalDateTime? = when (repetition) {
        ScheduleRepetition.NO_REPEAT -> null
        ScheduleRepetition.WEEKLY -> dateTime.plusWeeks(1)
        ScheduleRepetition.MONTHLY -> dateTime.plusMonths(1)
        ScheduleRepetition.BI_MONTHLY -> dateTime.plusMonths(2)
        ScheduleRepetition.YEARLY -> dateTime.plusYears(1)
    }

    override fun getPrevReminderFromDate(
        dateTime: LocalDateTime,
        repetition: ScheduleRepetition
    ): LocalDateTime? = when (repetition) {
        ScheduleRepetition.NO_REPEAT -> null
        ScheduleRepetition.WEEKLY -> dateTime.minusWeeks(1)
        ScheduleRepetition.MONTHLY -> dateTime.minusMonths(1)
        ScheduleRepetition.BI_MONTHLY -> dateTime.minusMonths(2)
        ScheduleRepetition.YEARLY -> dateTime.minusYears(1)
    }

    override suspend fun saveScheduleAndSetReminder(
        schedule: Schedule
    ) = withContext(Dispatchers.IO) {
        val insertedId = dao.upsert(schedule.toEntity()).first()
            .takeIf { it > RivoDatabase.DEFAULT_ID_LONG }
            ?: schedule.id
        scheduleReminder(schedule.copy(id = insertedId))
    }

    override fun scheduleReminder(schedule: Schedule) {
        scheduler.cancel(schedule.id)
        scheduler.setReminder(schedule)
        receiverService.toggleBootAndTimeSetReceivers(true)
    }

    override suspend fun createTransactionFromScheduleAndSetNextReminder(
        schedule: Schedule,
        dateTime: LocalDateTime
    ) = withContext(Dispatchers.IO) {
        db.withTransaction {
            txRepo.saveTransaction(
                amount = schedule.amount,
                note = schedule.note,
                timestamp = dateTime,
                type = schedule.type,
                tagId = schedule.tagId,
                folderId = schedule.folderId,
                scheduleId = schedule.id,
                excluded = false
            )
            val nextReminderDate = schedule.nextPaymentTimestamp
                ?.let { getNextReminderFromDate(it, schedule.repetition) }
            saveScheduleAndSetReminder(
                schedule = schedule.copy(
                    nextPaymentTimestamp = nextReminderDate,
                    lastPaymentTimestamp = dateTime
                )
            )
        }
    }

    override suspend fun getLastTransactionTimestampForSchedule(
        id: Long
    ): LocalDateTime? = withContext(Dispatchers.IO) {
        dao.getLastTransactionTimestampForSchedule(id)
    }

    override suspend fun deleteScheduleById(id: Long) = withContext(Dispatchers.IO) {
        val entity = dao.getScheduleById(id) ?: return@withContext
        cancelSchedule(entity.toSchedule())
        dao.delete(entity)
    }

    override suspend fun cancelSchedule(schedule: Schedule) {
        scheduler.cancel(schedule.id)
    }

    override suspend fun setAllFutureScheduleReminders() = withContext(Dispatchers.IO) {
        dao.getAllSchedulesAfterTimestamp(DateUtil.now())
            .map(ScheduleEntity::toSchedule)
            .forEach(scheduler::setReminder)
    }

    override suspend fun deleteSchedulesByIds(ids: Set<Long>) {
        withContext(Dispatchers.IO) {
            trySuspend {
                ids.forEach { scheduler.cancel(it) }
                dao.deleteSchedulesById(ids)
            }
        }
    }
}