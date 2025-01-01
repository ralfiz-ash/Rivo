package dev.ridill.rivo.transactions.domain.model

import android.os.Parcelable
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.LocaleUtil
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import java.util.Currency

@Parcelize
data class Transaction(
    val id: Long,
    val amount: String,
    val note: String,
    val currency: Currency,
    val timestamp: LocalDateTime,
    val type: TransactionType,
    val tagId: Long?,
    val folderId: Long?,
    val scheduleId: Long?,
    val excluded: Boolean
) : Parcelable {
    companion object {
        val DEFAULT = Transaction(
            id = RivoDatabase.DEFAULT_ID_LONG,
            amount = String.Empty,
            note = String.Empty,
            timestamp = DateUtil.now(),
            type = TransactionType.DEBIT,
            currency = LocaleUtil.defaultCurrency,
            tagId = null,
            folderId = null,
            excluded = false,
            scheduleId = null
        )
    }
}