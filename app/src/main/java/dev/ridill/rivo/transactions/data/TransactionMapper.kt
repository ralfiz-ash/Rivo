package dev.ridill.rivo.transactions.data

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView
import dev.ridill.rivo.transactions.domain.model.FolderIndicator
import dev.ridill.rivo.transactions.domain.model.TagIndicator
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionListItem

fun TransactionEntity.toTransaction(): Transaction = Transaction(
    id = id,
    amount = TextFormat.number(
        value = amount,
        isGroupingUsed = false,
        maxFractionDigits = Int.MAX_VALUE
    ),
    note = note,
    timestamp = timestamp,
    type = type,
    folderId = folderId,
    tagId = tagId,
    excluded = isExcluded,
    scheduleId = scheduleId
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    note = note,
    amount = TextFormat.parseNumber(amount).orZero(),
    timestamp = timestamp,
    type = type,
    isExcluded = excluded,
    tagId = tagId,
    folderId = folderId,
    scheduleId = scheduleId
)

fun TransactionDetailsView.toTransactionListItem(): TransactionListItem {
    val tag = if (tagId != null
        && tagName != null
        && tagColorCode != null
        && tagCreatedTimestamp != null
    ) TagIndicator(
        id = tagId,
        name = tagName,
        color = Color(tagColorCode)
    ) else null

    val folder = if (folderId != null
        && folderName != null
        && folderCreatedTimestamp != null
    ) FolderIndicator(
        id = folderId,
        name = folderName,
    ) else null

    return TransactionListItem(
        id = transactionId,
        note = transactionNote,
        amount = transactionAmount,
        timestamp = transactionTimestamp,
        type = transactionType,
        excluded = excluded,
        tag = tag,
        folder = folder,
        scheduleId = scheduleId
    )
}

fun TransactionListItem.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    note = note,
    amount = amount,
    timestamp = timestamp,
    type = type,
    isExcluded = excluded,
    tagId = tag?.id,
    folderId = folder?.id,
    scheduleId = scheduleId
)