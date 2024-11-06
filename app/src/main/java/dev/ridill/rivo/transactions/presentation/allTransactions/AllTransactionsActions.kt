package dev.ridill.rivo.transactions.presentation.allTransactions

import dev.ridill.rivo.transactions.domain.model.AllTransactionsMultiSelectionOption
import dev.ridill.rivo.transactions.domain.model.TransactionTypeFilter

interface AllTransactionsActions {
    fun onSearchQueryChange(value: String)
    fun onClearAllFiltersClick()
    fun onDateFilterRangeChange(range: ClosedFloatingPointRange<Float>)
    fun onDateFilterClear()
    fun onTypeFilterSelect(filter: TransactionTypeFilter)
    fun onShowExcludedToggle(showExcluded: Boolean)
    fun onChangeTagFiltersClick()
    fun onClearTagFilterClick()
    fun onTransactionLongPress(id: Long)
    fun onTransactionSelectionChange(id: Long)
    fun onDismissMultiSelectionMode()
    fun onMultiSelectionOptionsClick()
    fun onMultiSelectionOptionsDismiss()
    fun onMultiSelectionOptionSelect(option: AllTransactionsMultiSelectionOption)
    fun onDeleteTransactionDismiss()
    fun onDeleteTransactionConfirm()
    fun onAggregationDismiss()
    fun onAggregationConfirm()
    fun onFilterOptionsClick()
    fun onFilterOptionsDismiss()
}