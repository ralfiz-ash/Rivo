package dev.ridill.rivo.settings.domain.modal

import android.os.Parcelable
import dev.ridill.rivo.core.domain.util.DateUtil
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data class BackupDetails(
    val name: String,
    val id: String,
    val timestamp: String,
    val hashSalt: String?
) : Parcelable {
    fun getParsedTimestamp(): LocalDateTime? = DateUtil.parseDateTimeOrNull(timestamp)
}