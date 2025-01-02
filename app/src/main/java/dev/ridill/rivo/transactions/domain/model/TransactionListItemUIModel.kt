package dev.ridill.rivo.transactions.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Currency

sealed class TransactionListItemUIModel {
    data class TransactionItem(
        val id: Long,
        val note: String,
        val amount: Double,
        val currency: Currency,
        val timestamp: LocalDateTime,
        val type: TransactionType,
        val excluded: Boolean,
        val tag: TagIndicator?,
        val folder: FolderIndicator?,
        val scheduleId: Long?
    ) : TransactionListItemUIModel() {
        constructor(transactionListItem: TransactionListItem) : this(
            transactionListItem.id,
            transactionListItem.note,
            transactionListItem.amount,
            transactionListItem.currency,
            transactionListItem.timestamp,
            transactionListItem.type,
            transactionListItem.excluded,
            transactionListItem.tag,
            transactionListItem.folder,
            transactionListItem.scheduleId
        )
    }

    data class DateSeparator(val date: LocalDate) : TransactionListItemUIModel()
}