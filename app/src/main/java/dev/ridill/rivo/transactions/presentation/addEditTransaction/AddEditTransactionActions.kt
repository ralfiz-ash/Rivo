package dev.ridill.rivo.transactions.presentation.addEditTransaction

import dev.ridill.rivo.schedules.domain.model.ScheduleRepetition
import dev.ridill.rivo.transactions.domain.model.TransactionType

interface AddEditTransactionActions {
    fun onAmountChange(value: String)
    fun onAmountFocusLost()
    fun onEvaluateExpressionClick()
    fun onNoteChange(value: String)
    fun onRecommendedAmountClick(amount: Long)
    fun onTagSelect(tagId: Long)
    fun onViewAllTagsClick()
    fun onTimestampClick()
    fun onDateSelectionDismiss()
    fun onDateSelectionConfirm(millis: Long)
    fun onPickTimeClick()
    fun onTimeSelectionDismiss()
    fun onTimeSelectionConfirm(hour: Int, minute: Int)
    fun onPickDateClick()
    fun onTypeChange(type: TransactionType)
    fun onExclusionToggle(excluded: Boolean)
    fun onDeleteClick()
    fun onDeleteDismiss()
    fun onDeleteConfirm()
    fun onSelectFolderClick()
    fun onScheduleModeToggleClick()
    fun onRepeatModeClick()
    fun onRepeatModeDismiss()
    fun onRepetitionSelect(repetition: ScheduleRepetition)
    fun onSaveClick()
}