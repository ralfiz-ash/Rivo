package dev.ridill.rivo.schedules.domain.model

import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.util.Currency

data class ActiveSchedule(
    val id: Long,
    val note: String?,
    val amount: Double,
    val currency: Currency,
    val type: TransactionType,
) {
    val amountFormatted: String
        get() = TextFormat.currency(amount, currency)
}