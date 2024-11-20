package dev.ridill.rivo.schedules.domain.model

import androidx.compose.runtime.Composable
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactions.domain.model.TransactionType

data class ActiveSchedule(
    val id: Long,
    val note: String?,
    val amount: Double,
    val type: TransactionType,
) {
    val amountFormatted: String
        @Composable
        get() = TextFormat.currencyAmount(amount)
}