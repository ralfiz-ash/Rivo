package dev.ridill.rivo.transactions.presentation.addEditTransaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.service.ExpEvalService
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.core.domain.util.WhiteSpace
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.domain.util.ifInfinite
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.navigation.destinations.AddEditTransactionScreenSpec
import dev.ridill.rivo.core.ui.navigation.destinations.AddEditTxResult
import dev.ridill.rivo.core.ui.navigation.destinations.NavDestination
import dev.ridill.rivo.core.ui.navigation.destinations.TransformationResult
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.schedules.data.toTransaction
import dev.ridill.rivo.schedules.domain.model.ScheduleRepetition
import dev.ridill.rivo.tags.domain.repository.TagsRepository
import dev.ridill.rivo.transactions.domain.model.AmountTransformation
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditTransactionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val transactionRepo: AddEditTransactionRepository,
    tagsRepo: TagsRepository,
    private val evalService: ExpEvalService,
    private val eventBus: EventBus<AddEditTransactionEvent>
) : ViewModel(), AddEditTransactionActions {
    private val transactionIdArg = AddEditTransactionScreenSpec
        .getTransactionIdFromSavedStateHandle(savedStateHandle)

    private val linkFolderIdArg = AddEditTransactionScreenSpec
        .getFolderIdToLinkFromSavedStateHandle(savedStateHandle)

    private val scheduleModeArg = AddEditTransactionScreenSpec
        .getIsScheduleModeFromSavedStateHandle(savedStateHandle)

    private val isLoading = MutableStateFlow(false)

    private val coercedIdArg: Long
        get() = transactionIdArg.coerceAtLeast(RivoDatabase.DEFAULT_ID_LONG)

    private val isScheduleTxMode = savedStateHandle.getStateFlow(IS_SCHEDULE_MODE, false)

    private val txInput = savedStateHandle.getStateFlow(TX_INPUT, Transaction.DEFAULT)
    val amountInput = txInput.mapLatest { it.amount }
        .asStateFlow(viewModelScope, String.Empty)

    private val isAmountInputAnExpression = amountInput.mapLatest { evalService.isExpression(it) }
        .distinctUntilChanged()

    val noteInput = txInput.mapLatest { it.note }

    private val selectedTagId = txInput.mapLatest { it.tagId }
        .distinctUntilChanged()

    private val timestamp = txInput.mapLatest { it.timestamp }
        .distinctUntilChanged()

    private val transactionFolderId = txInput.mapLatest { it.folderId }
        .distinctUntilChanged()

    private val transactionType = txInput.mapLatest { it.type }
        .distinctUntilChanged()

    private val isTransactionExcluded = txInput.mapLatest { it.excluded }
        .distinctUntilChanged()

    val recentTagsPagingData = tagsRepo.getAllTagsPagingData(
        searchQuery = String.Empty,
        limit = UtilConstants.DEFAULT_TAG_LIST_LIMIT
    ).cachedIn(viewModelScope)

    private val showDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    private val amountRecommendations = transactionRepo.getAmountRecommendations()

    private val showDatePicker = savedStateHandle.getStateFlow(SHOW_DATE_PICKER, false)
    private val showTimePicker = savedStateHandle.getStateFlow(SHOW_TIME_PICKER, false)

    private val linkedFolderName = transactionFolderId.flatMapLatest { selectedId ->
        transactionRepo.getFolderNameForId(selectedId)
    }.distinctUntilChanged()

    private val showRepetitionSelection = savedStateHandle
        .getStateFlow(SHOW_REPETITION_SELECTION, false)
    private val selectedRepetition = savedStateHandle
        .getStateFlow(SELECTED_REPETITION, ScheduleRepetition.NO_REPEAT)

    private val duplicateMode = savedStateHandle.getStateFlow(DUPLICATE_MODE, false)
    private val menuOptions = combineTuple(
        duplicateMode,
        isScheduleTxMode
    ).mapLatest { (duplicateMode, scheduleMode) ->
        var optionEntries = AddEditTxOption.entries.toSet()

        if (transactionIdArg == NavDestination.ARG_INVALID_ID_LONG) {
            optionEntries = optionEntries - AddEditTxOption.DELETE
            optionEntries = optionEntries - AddEditTxOption.DUPLICATE
        }

        if (duplicateMode) {
            optionEntries = optionEntries - AddEditTxOption.DUPLICATE
        }

        optionEntries = if (scheduleMode) optionEntries - AddEditTxOption.CONVERT_TO_SCHEDULE
        else optionEntries - AddEditTxOption.CONVERT_TO_NORMAL_TRANSACTION

        optionEntries
    }

    val state = combineTuple(
        isLoading,
        menuOptions,
        transactionType,
        isAmountInputAnExpression,
        amountRecommendations,
        timestamp,
        showDatePicker,
        showTimePicker,
        isTransactionExcluded,
        selectedTagId,
        showDeleteConfirmation,
        linkedFolderName,
        isScheduleTxMode,
        selectedRepetition,
        showRepetitionSelection
    ).mapLatest { (
                      isLoading,
                      menuOptions,
                      transactionType,
                      isAmountInputAnExpression,
                      amountRecommendations,
                      timestamp,
                      showDatePicker,
                      showTimePicker,
                      isTransactionExcluded,
                      selectedTagId,
                      showDeleteConfirmation,
                      linkedFolderName,
                      isScheduleTxMode,
                      selectedRepetition,
                      showRepetitionSelection
                  ) ->
        AddEditTransactionState(
            isLoading = isLoading,
            menuOptions = menuOptions,
            transactionType = transactionType,
            isAmountInputAnExpression = isAmountInputAnExpression,
            amountRecommendations = amountRecommendations,
            timestamp = timestamp,
            showDatePicker = showDatePicker,
            showTimePicker = showTimePicker,
            isTransactionExcluded = isTransactionExcluded,
            selectedTagId = selectedTagId,
            showDeleteConfirmation = showDeleteConfirmation,
            linkedFolderName = linkedFolderName,
            isScheduleTxMode = isScheduleTxMode,
            selectedRepetition = selectedRepetition,
            showRepeatModeSelection = showRepetitionSelection
        )
    }.asStateFlow(viewModelScope, AddEditTransactionState())

    val events = eventBus.eventFlow

    init {
        onInit()
    }

    private fun onInit() = viewModelScope.launch {
        val transaction: Transaction = if (scheduleModeArg) {
            val schedule = transactionRepo.getScheduleById(transactionIdArg)
            savedStateHandle[SELECTED_REPETITION] = schedule?.repetition
                ?: ScheduleRepetition.NO_REPEAT

            schedule?.toTransaction(
                dateTime = schedule.nextPaymentTimestamp
                    ?: DateUtil.now()
                        .plusDays(1L),
                txId = transactionIdArg
            )
        } else {
            transactionRepo.getTransactionById(transactionIdArg)
        } ?: Transaction.DEFAULT
        savedStateHandle[IS_SCHEDULE_MODE] = scheduleModeArg
        val dateNow = DateUtil.now()
        val timestamp = if (isScheduleTxMode.value && transaction.timestamp <= dateNow)
            dateNow.plusDays(1)
        else transaction.timestamp

        savedStateHandle[TX_INPUT] = transaction.copy(
            folderId = linkFolderIdArg ?: transaction.folderId,
            timestamp = timestamp
        )
    }

    override fun onAmountChange(value: String) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(amount = value)
    }

    override fun onAmountFocusLost() {
        evaluateAmountInput()
    }

    override fun onEvaluateExpressionClick() {
        evaluateAmountInput()
    }

    private fun evaluateAmountInput() {
        val amountInput = amountInput.value
            .trim()
            .ifEmpty { return }

        val isExpression = evalService.isExpression(amountInput)
        val result = if (isExpression) evalService.evalOrNull(amountInput)
        else TextFormat.parseNumber(amountInput)
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            amount = TextFormat.number(
                value = result.orZero(),
                isGroupingUsed = false
            )
        )
    }

    override fun onNoteChange(value: String) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(note = value)
    }

    override fun onRecommendedAmountClick(amount: Long) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            amount = TextFormat.number(
                value = amount,
                isGroupingUsed = false
            )
        )
    }

    override fun onTagSelect(tagId: Long) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            tagId = tagId.takeIf { it != txInput.value.tagId }
        )
    }

    override fun onViewAllTagsClick() {
        viewModelScope.launch {
            eventBus.send(AddEditTransactionEvent.LaunchTagSelection(txInput.value.tagId))
        }
    }

    override fun onTimestampClick() {
        savedStateHandle[SHOW_DATE_PICKER] = true
    }

    override fun onDateSelectionDismiss() {
        savedStateHandle[SHOW_DATE_PICKER] = false
    }

    override fun onDateSelectionConfirm(millis: Long) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            timestamp = DateUtil.dateFromMillisWithTime(
                millis = millis,
                time = txInput.value.timestamp
            )
        )
        savedStateHandle[SHOW_DATE_PICKER] = false
    }

    override fun onPickTimeClick() {
        savedStateHandle[SHOW_DATE_PICKER] = false
        savedStateHandle[SHOW_TIME_PICKER] = true
    }

    override fun onTimeSelectionDismiss() {
        savedStateHandle[SHOW_TIME_PICKER] = false
    }

    override fun onTimeSelectionConfirm(hour: Int, minute: Int) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            timestamp = txInput.value.timestamp
                .withHour(hour)
                .withMinute(minute)
        )
        savedStateHandle[SHOW_TIME_PICKER] = false
    }

    override fun onPickDateClick() {
        savedStateHandle[SHOW_TIME_PICKER] = false
        savedStateHandle[SHOW_DATE_PICKER] = true
    }

    override fun onTypeChange(type: TransactionType) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            type = type
        )
    }

    override fun onExclusionToggle(excluded: Boolean) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            excluded = excluded
        )
    }

    fun onAmountTransformationResult(result: TransformationResult) {
        val amount = amountInput.value.toDoubleOrNull() ?: return
        val transformedAmount = when (result.transformation) {
            AmountTransformation.DIVIDE_BY -> amount / result.factor.toDoubleOrNull().orZero()
            AmountTransformation.MULTIPLIER -> amount * result.factor.toDoubleOrNull().orZero()
            AmountTransformation.PERCENT -> amount * (result.factor.toFloatOrNull().orZero() / 100f)
        }
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            amount = TextFormat.number(
                value = transformedAmount.ifInfinite { Double.Zero },
                isGroupingUsed = false
            )
        )
    }

    override fun onOptionClick(option: AddEditTxOption) {
        when (option) {
            AddEditTxOption.DELETE -> {
                savedStateHandle[SHOW_DELETE_CONFIRMATION] = true
            }

            AddEditTxOption.CONVERT_TO_SCHEDULE -> {
                toggleScheduling(true)
            }

            AddEditTxOption.CONVERT_TO_NORMAL_TRANSACTION -> {
                toggleScheduling(false)
            }

            AddEditTxOption.DUPLICATE -> {
                enableDuplicateMode()
            }
        }
    }

    private var enableDuplicateModeJob: Job? = null
    private fun enableDuplicateMode() {
        enableDuplicateModeJob?.cancel()
        enableDuplicateModeJob = viewModelScope.launch {
            savedStateHandle[DUPLICATE_MODE] = true
            val txInput = txInput.value
            savedStateHandle[TX_INPUT] = txInput.copy(
                id = RivoDatabase.DEFAULT_ID_LONG,
                note = buildString {
                    append(txInput.note)
                    append(String.WhiteSpace)
                    append("(Copy)")
                }
            )

            eventBus.send(AddEditTransactionEvent.StartDuplicateModeAnim)
        }
    }

    override fun onDeleteDismiss() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
    }

    override fun onDeleteConfirm() {
        viewModelScope.launch {
            isLoading.update { true }
            if (isScheduleTxMode.value)
                transactionRepo.deleteSchedule(coercedIdArg)
            else
                transactionRepo.deleteTransaction(coercedIdArg)
            isLoading.update { false }
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            eventBus.send(AddEditTransactionEvent.NavigateUpWithResult(AddEditTxResult.TRANSACTION_DELETED))
        }
    }

    override fun onSelectFolderClick() {
        viewModelScope.launch {
            eventBus.send(AddEditTransactionEvent.LaunchFolderSelection(txInput.value.folderId))
        }
    }

    fun onFolderSelectionResult(id: Long) {
        savedStateHandle[TX_INPUT] = txInput.value.copy(
            folderId = id.takeIf { it != NavDestination.ARG_INVALID_ID_LONG }
        )
    }

    private fun toggleScheduling(enable: Boolean) {
        savedStateHandle[IS_SCHEDULE_MODE] = enable
        if (enable) {
            if (txInput.value.timestamp <= DateUtil.now()) {
                savedStateHandle[TX_INPUT] = txInput.value
                    .copy(timestamp = DateUtil.now().plusDays(1))
            }
        } else {
            savedStateHandle[TX_INPUT] = txInput.value
                .copy(timestamp = DateUtil.now())
        }
    }

    override fun onRepeatModeClick() {
        savedStateHandle[SHOW_REPETITION_SELECTION] = true
    }

    override fun onRepeatModeDismiss() {
        savedStateHandle[SHOW_REPETITION_SELECTION] = false
    }

    override fun onRepetitionSelect(repetition: ScheduleRepetition) {
        savedStateHandle[SELECTED_REPETITION] = repetition
        savedStateHandle[SHOW_REPETITION_SELECTION] = false
    }

    override fun onSaveClick() {
        viewModelScope.launch {
            val txInput = txInput.value
            val amountInput = txInput.amount.trim()
            if (amountInput.isEmpty()) {
                eventBus.send(
                    AddEditTransactionEvent.ShowUiMessage(
                        UiText.StringResource(R.string.error_invalid_amount, true)
                    )
                )
                return@launch
            }
            val isExp = evalService.isExpression(amountInput)
            val evaluatedAmount = (if (isExp) evalService.evalOrNull(amountInput)
            else TextFormat.parseNumber(amountInput)) ?: -1.0
            if (evaluatedAmount < Double.Zero) {
                eventBus.send(
                    AddEditTransactionEvent.ShowUiMessage(
                        UiText.StringResource(
                            R.string.error_invalid_amount,
                            true
                        )
                    )
                )
                return@launch
            }
            isLoading.update { true }
            var scheduleOrTxIdForInsertion = txInput.id
            if (isScheduleTxMode.value) {
                // Saving schedule
                // scheduleModeArg = false means this input started off as a transaction
                // and is being changed to a schedule now
                // so delete the initial transaction before saving the schedule
                // and reset the id to Default value to save new schedule
                if (!scheduleModeArg) {
                    transactionRepo.deleteTransaction(txInput.id)
                    scheduleOrTxIdForInsertion = RivoDatabase.DEFAULT_ID_LONG
                }
                transactionRepo.saveAsSchedule(
                    transaction = txInput.copy(
                        id = scheduleOrTxIdForInsertion,
                        amount = evaluatedAmount.toString()
                    ),
                    repetition = selectedRepetition.value
                )
                isLoading.update { false }
                eventBus.send(AddEditTransactionEvent.NavigateUpWithResult(AddEditTxResult.SCHEDULE_SAVED))
            } else {
                // Saving transaction
                // scheduleModeArg = true means this input started off as a schedule
                // and is being changed to a transaction now
                // so delete the initial schedule before saving the transaction
                // and reset the id to Default value to save new transaction
                var linkedScheduleId = txInput.scheduleId
                if (scheduleModeArg) {
                    transactionRepo.deleteSchedule(txInput.id)
                    linkedScheduleId = null
                    scheduleOrTxIdForInsertion = RivoDatabase.DEFAULT_ID_LONG
                }
                transactionRepo.saveTransaction(
                    transaction = txInput.copy(
                        id = scheduleOrTxIdForInsertion,
                        amount = evaluatedAmount.toString(),
                        scheduleId = linkedScheduleId
                    )
                )
                isLoading.update { false }
                eventBus.send(AddEditTransactionEvent.NavigateUpWithResult(AddEditTxResult.TRANSACTION_SAVED))
            }
        }
    }

    sealed interface AddEditTransactionEvent {
        data class ShowUiMessage(val uiText: UiText) : AddEditTransactionEvent
        data class NavigateUpWithResult(val result: AddEditTxResult) : AddEditTransactionEvent
        data class LaunchFolderSelection(val preselectedId: Long?) : AddEditTransactionEvent
        data class LaunchTagSelection(val preselectedId: Long?) : AddEditTransactionEvent
        data object StartDuplicateModeAnim : AddEditTransactionEvent
    }
}

private const val IS_SCHEDULE_MODE = "IS_SCHEDULE_MODE"
private const val TX_INPUT = "TX_INPUT"
private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"
private const val SHOW_DATE_PICKER = "SHOW_DATE_PICKER"
private const val SHOW_TIME_PICKER = "SHOW_TIME_PICKER"
private const val SHOW_REPETITION_SELECTION = "SHOW_REPETITION_SELECTION"
private const val SELECTED_REPETITION = "SELECTED_REPETITION"
private const val DUPLICATE_MODE = "DUPLICATE_MODE"