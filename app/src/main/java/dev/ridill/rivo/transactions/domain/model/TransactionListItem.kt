package dev.ridill.rivo.transactions.domain.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.core.ui.util.TextFormat
import java.time.LocalDate
import java.time.LocalDateTime

data class TransactionListItem(
    val id: Long,
    val note: String,
    val amount: Double,
    val timestamp: LocalDateTime,
    val type: TransactionType,
    val excluded: Boolean,
    val tag: TagIndicator?,
    val folder: FolderIndicator?,
    val scheduleId: Long?
) {
    val date: LocalDate
        get() = timestamp.toLocalDate()

    val amountFormatted: String
        @Composable
        get() = TextFormat.currencyAmount(amount)
}

data class TagIndicator(
    val id: Long,
    val name: String,
    val color: Color
)

data class FolderIndicator(
    val id: Long,
    val name: String,
)