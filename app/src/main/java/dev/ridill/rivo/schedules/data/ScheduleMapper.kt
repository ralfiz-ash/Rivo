package dev.ridill.rivo.schedules.data

import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import dev.ridill.rivo.schedules.domain.model.ActiveSchedule
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.transactions.domain.model.Transaction
import java.time.LocalDateTime

fun ScheduleEntity.toSchedule(): Schedule = Schedule(
    id = id,
    repetition = repetition,
    nextReminderDate = nextReminderTimestamp,
    amount = amount,
    note = note,
    type = type,
    tagId = tagId,
    folderId = folderId,
    lastPaidDate = lastPaymentTimestamp
)

fun Schedule.toTransaction(
    dateTime: LocalDateTime = DateUtil.now(),
    txId: Long = RivoDatabase.DEFAULT_ID_LONG
): Transaction = Transaction(
    id = txId,
    amount = amount.toString(),
    note = note.orEmpty(),
    timestamp = dateTime,
    type = type,
    tagId = tagId,
    folderId = folderId,
    excluded = false,
    scheduleId = id
)

fun Schedule.toEntity(): ScheduleEntity = ScheduleEntity(
    id = id,
    amount = amount,
    note = note,
    type = type,
    repetition = repetition,
    tagId = tagId,
    folderId = folderId,
    nextReminderTimestamp = nextReminderDate,
    lastPaymentTimestamp = lastPaidDate
)

fun ScheduleEntity.toActiveSchedule(): ActiveSchedule = ActiveSchedule(
    id = id,
    note = note?.let { UiText.DynamicString(it) }
        ?: UiText.StringResource(type.labelRes),
    amount = amount,
    dueDate = nextReminderTimestamp ?: DateUtil.now()
)