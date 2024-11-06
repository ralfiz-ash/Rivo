package dev.ridill.rivo.folders.presentation.folderDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.zhuinden.flowcombinetuplekt.combineTuple
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.core.domain.util.asStateFlow
import dev.ridill.rivo.core.domain.util.orFalse
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.navigation.destinations.FolderDetailsScreenSpec
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.folders.domain.model.AggregateType
import dev.ridill.rivo.folders.domain.repository.FolderDetailsRepository
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FolderDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: FolderDetailsRepository,
    private val eventBus: EventBus<FolderDetailsEvent>
) : ViewModel(), FolderDetailsActions {

    private val folderIdArg = FolderDetailsScreenSpec
        .getFolderIdArgFromSavedStateHandle(savedStateHandle)

    private val folderIdFlow = MutableStateFlow(folderIdArg)
    private val folderDetails = repo.getFolderDetailsById(folderIdArg)
    private val folderName = folderDetails
        .mapLatest { it?.name.orEmpty() }
        .distinctUntilChanged()
    private val createdTimestamp = folderDetails
        .mapLatest { it?.createdTimestamp ?: DateUtil.now() }
        .distinctUntilChanged()
    private val excluded = folderDetails
        .mapLatest { it?.excluded.orFalse() }
        .distinctUntilChanged()
    private val aggregateAmount = folderDetails
        .mapLatest { it?.aggregate.orZero() }
        .distinctUntilChanged()
    private val aggregateType = folderDetails
        .map { it?.aggregateType ?: AggregateType.BALANCED }
        .distinctUntilChanged()

    val transactionPagingData = repo.getTransactionsInFolderPaged(folderIdArg)
        .cachedIn(viewModelScope)

    private val showDeleteConfirmation = savedStateHandle
        .getStateFlow(SHOW_DELETE_CONFIRMATION, false)

    val state = combineTuple(
        folderName,
        createdTimestamp,
        excluded,
        aggregateAmount,
        aggregateType,
        showDeleteConfirmation
    ).map { (
                name,
                createdTimestamp,
                excluded,
                aggregateAmount,
                aggregateType,
                showDeleteConfirmation
            ) ->
        FolderDetailsState(
            folderName = name,
            createdTimestamp = createdTimestamp,
            isExcluded = excluded,
            aggregateAmount = aggregateAmount,
            aggregateType = aggregateType,
            showDeleteConfirmation = showDeleteConfirmation
        )
    }.asStateFlow(viewModelScope, FolderDetailsState())

    val events = eventBus.eventFlow

    override fun onDeleteClick() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = true
    }

    override fun onDeleteDismiss() {
        savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
    }

    override fun onDeleteFolderOnlyClick() {
        viewModelScope.launch {
            val id = folderIdFlow.value
            repo.deleteFolderById(id)
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            eventBus.send(FolderDetailsEvent.FolderDeleted)
        }
    }

    override fun onDeleteFolderAndTransactionsClick() {
        viewModelScope.launch {
            val id = folderIdFlow.value
            repo.deleteFolderWithTransactions(id)
            savedStateHandle[SHOW_DELETE_CONFIRMATION] = false
            eventBus.send(FolderDetailsEvent.FolderDeleted)
        }
    }

    override fun onTransactionSwipeToDismiss(transaction: TransactionListItemUIModel.TransactionItem) {
        viewModelScope.launch {
            repo.removeTransactionFromFolderById(transaction.id)
            eventBus.send(FolderDetailsEvent.TransactionRemovedFromGroup(transaction))
        }
    }

    fun onRemoveTransactionUndo(transaction: TransactionListItemUIModel.TransactionItem) = viewModelScope.launch {
        repo.addTransactionToFolder(transaction)
    }

    sealed interface FolderDetailsEvent {
        data class ShowUiMessage(val uiText: UiText) : FolderDetailsEvent
        data object FolderDeleted : FolderDetailsEvent
        data class TransactionRemovedFromGroup(val transaction: TransactionListItemUIModel.TransactionItem) :
            FolderDetailsEvent
    }
}

private const val SHOW_DELETE_CONFIRMATION = "SHOW_DELETE_CONFIRMATION"