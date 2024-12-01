package dev.ridill.rivo.settings.presentation.currencyUpdate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.EventBus
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Currency
import javax.inject.Inject

@HiltViewModel
class UpdateCurrencyPreferenceViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: CurrencyRepository,
    private val eventBus: EventBus<UpdateCurrencyEvent>
) : ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY, String.Empty)

    val currencyPagingData = searchQuery.flatMapLatest { query ->
        repo.getCurrencyListPaged(query)
    }.cachedIn(viewModelScope)

    val events = eventBus.eventFlow

    fun onSearchQueryChange(value: String) {
        savedStateHandle[SEARCH_QUERY] = value
    }

    fun onConfirm(currency: Currency) = viewModelScope.launch {
        repo.saveCurrencyPreference(currency)
        eventBus.send(UpdateCurrencyEvent.CurrencyUpdated)
    }

    sealed interface UpdateCurrencyEvent {
        data object CurrencyUpdated : UpdateCurrencyEvent
    }
}

private const val SEARCH_QUERY = "SEARCH_QUERY"