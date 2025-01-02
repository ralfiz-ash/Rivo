package dev.ridill.rivo.core.data.db

import androidx.core.database.getStringOrNull
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.folders.data.local.FolderDao
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.views.FolderAndAggregateView
import dev.ridill.rivo.schedules.data.local.SchedulesDao
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import dev.ridill.rivo.settings.data.local.BudgetPreferenceDao
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.local.CurrencyListDao
import dev.ridill.rivo.settings.data.local.CurrencyPreferenceDao
import dev.ridill.rivo.settings.data.local.entity.BudgetPreferenceEntity
import dev.ridill.rivo.settings.data.local.entity.ConfigEntity
import dev.ridill.rivo.settings.data.local.entity.CurrencyListEntity
import dev.ridill.rivo.settings.data.local.entity.CurrencyPreferenceEntity
import dev.ridill.rivo.tags.data.local.TagsDao
import dev.ridill.rivo.tags.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView

@Database(
    entities = [
        BudgetPreferenceEntity::class,
        TransactionEntity::class,
        TagEntity::class,
        FolderEntity::class,
        ScheduleEntity::class,
        CurrencyListEntity::class,
        CurrencyPreferenceEntity::class,
        ConfigEntity::class
    ],
    views = [
        TransactionDetailsView::class,
        FolderAndAggregateView::class
    ],
    version = 2
)
@TypeConverters(DateTimeConverter::class)
abstract class RivoDatabase : RoomDatabase() {
    companion object {
        const val NAME = "Rivo.db"
        const val DEFAULT_ID_LONG = 0L
        const val INVALID_LIMIT = -1
    }

    // Dao Methods
    abstract fun budgetPreferenceDao(): BudgetPreferenceDao
    abstract fun transactionDao(): TransactionDao
    abstract fun tagsDao(): TagsDao
    abstract fun folderDao(): FolderDao
    abstract fun schedulesDao(): SchedulesDao
    abstract fun currencyListDao(): CurrencyListDao
    abstract fun currencyPreferenceDao(): CurrencyPreferenceDao
    abstract fun configDao(): ConfigDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE transaction_table ADD COLUMN currency_code TEXT NOT NULL")

        updateTransactions(db)
        updateSchedules(db)
    }

    private fun updateTransactions(db: SupportSQLiteDatabase) {
        db.query("SELECT id, timestamp FROM transaction_table").use { cursor ->
            val id = cursor.getLong(0)
            val timestamp = cursor.getString(1)
            db.query(
                """SELECT currency_code
        FROM currency_preference_table
        WHERE DATE(date) <= DATE($timestamp)
        ORDER BY DATE(date) DESC
        LIMIT 1"""
            ).use { currencyCursor ->
                val currencyCode = currencyCursor.getStringOrNull(0)
                    ?: LocaleUtil.defaultCurrency.currencyCode
                db.execSQL("UPDATE transaction_table SET currency_code = $currencyCode WHERE id = $id")
            }
        }
    }

    private fun updateSchedules(db: SupportSQLiteDatabase) {
        db.query("SELECT id, last_payment_timestamp FROM schedules_table").use { cursor ->
            val id = cursor.getLong(0)
            val timestamp = cursor.getStringOrNull(1) ?: DateUtil.now().toString()
            db.query(
                """SELECT currency_code
        FROM currency_preference_table
        WHERE DATE(date) <= DATE($timestamp)
        ORDER BY DATE(date) DESC
        LIMIT 1"""
            ).use { currencyCursor ->
                val currencyCode = currencyCursor.getStringOrNull(0)
                    ?: LocaleUtil.defaultCurrency.currencyCode
                db.execSQL("UPDATE schedules_table SET currency_code = $currencyCode WHERE id = $id")
            }
        }
    }
}