package dev.ridill.rivo.schedules.presentation.allSchedules

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.schedules.domain.repository.AllSchedulesRepository
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AllSchedulesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: AllSchedulesRepository,
    private val eventBus: EventBus<AllSchedulesEvent>
) : ViewModel(), AllSchedulesActions {

    private val showNotificationRationale = savedStateHandle
        .getStateFlow(SHOW_NOTIFICATION_RATIONALE, false)

    val schedulesPagingData = repo.getSchedulesPagingData()
        .cachedIn(viewModelScope)

    private val selectedScheduleIds = savedStateHandle
        .getStateFlow(SELECTED_SCHEDULE_IDS, emptySet<Long>())
    private val multiSelectionModeActive = selectedScheduleIds.map { it.isNotEmpty() }
        .distinctUntilChanged()
        .asStateFlow(viewModelScope, false)

    private val showDeleteSelectedSchedulesConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION, false)

    val state = combineTuple(
        showNotificationRationale,
        multiSelectionModeActive,
        selectedScheduleIds,
        showDeleteSelectedSchedulesConfirmation
    ).map { (
                showNotificationRationale,
                multiSelectionModeActive,
                selectedScheduleIds,
                showDeleteSelectedSchedulesConfirmation
            ) ->
        AllSchedulesState(
            showNotificationRationale = showNotificationRationale,
            multiSelectionModeActive = multiSelectionModeActive,
            selectedScheduleIds = selectedScheduleIds,
            showDeleteSelectedSchedulesConfirmation = showDeleteSelectedSchedulesConfirmation
        )
    }
        .asStateFlow(viewModelScope, AllSchedulesState())

    val events = eventBus.eventFlow

    fun refreshCurrentDate() = repo.refreshCurrentDate()

    override fun onNotificationWarningClick() {
        savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = true
    }

    override fun onNotificationRationaleDismiss() {
        savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = false
    }

    override fun onNotificationRationaleAgree() {
        viewModelScope.launch {
            savedStateHandle[SHOW_NOTIFICATION_RATIONALE] = false
            eventBus.send(AllSchedulesEvent.RequestNotificationPermission)
        }
    }

    override fun onMarkSchedulePaidClick(id: Long) {
        viewModelScope.launch {
            when (val resource = repo.markScheduleAsPaid(id)) {
                is Resource.Error -> {
                    resource.message?.let {
                        eventBus.send(AllSchedulesEvent.ShowUiMessage(it))
                    }
                }

                is Resource.Success -> {
                    eventBus.send(AllSchedulesEvent.ShowUiMessage(UiText.StringResource(R.string.schedule_marked_as_paid)))
                }
            }
        }
    }

    override fun onScheduleLongPress(id: Long) {
        savedStateHandle[SELECTED_SCHEDULE_IDS] = selectedScheduleIds.value + id
    }

    override fun onScheduleSelectionToggle(id: Long) {
        val selectedIds = selectedScheduleIds.value
        savedStateHandle[SELECTED_SCHEDULE_IDS] = if (id in selectedIds) selectedIds - id
        else selectedIds + id
    }

    override fun onMultiSelectionModeDismiss() {
        savedStateHandle[SELECTED_SCHEDULE_IDS] = emptySet<Long>()
    }

    override fun onDeleteSelectedSchedulesClick() {
        savedStateHandle[SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION] = true
    }

    override fun onDeleteSelectedSchedulesDismiss() {
        savedStateHandle[SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION] = false
    }

    override fun onDeleteSelectedSchedulesConfirm() {
        viewModelScope.launch {
            repo.deleteSchedulesById(selectedScheduleIds.value)
            savedStateHandle[SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION] = false
            savedStateHandle[SELECTED_SCHEDULE_IDS] = emptySet<Long>()
        }
    }

    sealed interface AllSchedulesEvent {
        data class ShowUiMessage(val uiText: UiText) : AllSchedulesEvent
        data object RequestNotificationPermission : AllSchedulesEvent
    }
}

private const val SHOW_NOTIFICATION_RATIONALE = "SHOW_NOTIFICATION_RATIONALE"
private const val SELECTED_SCHEDULE_IDS = "SELECTED_SCHEDULE_IDS"
private const val SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION =
    "SHOW_DELETE_SELECTED_SCHEDULES_CONFIRMATION"