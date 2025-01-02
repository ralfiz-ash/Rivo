package dev.ridill.rivo.settings.presentation.currencyUpdate

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@HiltViewModel
class CurrencySelectionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repo: CurrencyRepository
) : ViewModel() {

    val searchQuery = savedStateHandle.getStateFlow(SEARCH_QUERY, String.Empty)

    val currencyPagingData = searchQuery.flatMapLatest { query ->
        repo.getCurrencyListPaged(query)
    }.cachedIn(viewModelScope)

    fun onSearchQueryChange(value: String) {
        savedStateHandle[SEARCH_QUERY] = value
    }
}

private const val SEARCH_QUERY = "SEARCH_QUERY"