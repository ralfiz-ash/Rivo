package dev.ridill.rivo.schedules.domain.model

import androidx.compose.runtime.Composable
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

sealed class ScheduleListItemUiModel {
    data class ScheduleItem(
        val id: Long,
        val amount: Double,
        val note: String?,
        val type: TransactionType,
        val lastPaymentTimestamp: LocalDateTime?,
        val nextPaymentTimestamp: LocalDateTime?,
        val canMarkPaid: Boolean
    ) : ScheduleListItemUiModel() {
        constructor(
            scheduleItem: ScheduleEntity,
            canMarkPaid: Boolean
        ) : this(
            id = scheduleItem.id,
            amount = scheduleItem.amount,
            note = scheduleItem.note,
            type = scheduleItem.type,
            lastPaymentTimestamp = scheduleItem.lastPaymentTimestamp,
            nextPaymentTimestamp = scheduleItem.nextPaymentTimestamp,
            canMarkPaid = canMarkPaid
        )

        val amountFormatted: String
            @Composable
            get() = TextFormat.currencyAmount(amount)
    }

    data class TypeSeparator(val label: UiText) : ScheduleListItemUiModel()
}