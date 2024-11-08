package dev.ridill.rivo.transactions.presentation.allTransactions

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TransactionTypeFilter
import java.time.LocalDate

data class AllTransactionsState(
    val searchModeActive: Boolean = false,
    val dateLimitsFloatRange: ClosedFloatingPointRange<Float> = Float.Zero.rangeTo(Float.Zero),
    val dateRangeSteps: Long = Long.Zero,
    val selectedDateRange: ClosedFloatingPointRange<Float> = Float.Zero.rangeTo(Float.Zero),
    val selectedDates: Pair<LocalDate, LocalDate> = DateUtil.dateNow() to DateUtil.dateNow(),
    val selectedTransactionTypeFilter: TransactionTypeFilter = TransactionTypeFilter.ALL,
    val aggregateAmount: Double? = null,
    val transactionListLabel: UiText = UiText.DynamicString(String.Empty),
    val selectedTransactionIds: Set<Long> = emptySet(),
    val transactionMultiSelectionModeActive: Boolean = false,
    val showDeleteTransactionConfirmation: Boolean = false,
    val showExcludedTransactions: Boolean = false,
    val selectedTagFilters: List<Tag> = emptyList(),
    val showAggregationConfirmation: Boolean = false,
    val showMultiSelectionOptions: Boolean = false,
    val showFilterOptions: Boolean = false
)