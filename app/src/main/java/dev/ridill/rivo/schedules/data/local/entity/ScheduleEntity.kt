package dev.ridill.rivo.schedules.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.schedules.domain.model.ScheduleRepetition
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

@Entity(tableName = "schedules_table")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = RivoDatabase.DEFAULT_ID_LONG,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "note")
    val note: String?,

    @ColumnInfo(name = "type")
    val type: TransactionType,

    @ColumnInfo(name = "tag_id")
    val tagId: Long?,

    @ColumnInfo(name = "folder_id")
    val folderId: Long?,

    @ColumnInfo(name = "repetition")
    val repetition: ScheduleRepetition,

    @ColumnInfo(name = "last_payment_timestamp")
    val lastPaymentTimestamp: LocalDateTime?,

    @ColumnInfo(name = "next_payment_timestamp")
    val nextPaymentTimestamp: LocalDateTime?
)