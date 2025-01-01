package dev.ridill.rivo.settings.presentation.currencyUpdate

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.LabelledRadioButton
import dev.ridill.rivo.core.ui.components.ListSearchSheet
import dev.ridill.rivo.core.ui.navigation.destinations.CurrencySelectionSheetSpec
import java.util.Currency

@Composable
fun CurrencySelectionSheet(
    selectedCode: String?,
    searchQuery: () -> String,
    onSearchQueryChange: (String) -> Unit,
    currenciesPagingData: LazyPagingItems<Currency>,
    onDismiss: () -> Unit,
    onConfirm: (Currency) -> Unit,
    modifier: Modifier = Modifier
) {
    ListSearchSheet(
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        onDismiss = onDismiss,
        title = stringResource(CurrencySelectionSheetSpec.labelRes),
        placeholder = stringResource(R.string.search_currency),
        modifier = modifier
    ) {
        items(
            count = currenciesPagingData.itemCount,
            key = currenciesPagingData.itemKey { it.currencyCode },
            contentType = currenciesPagingData.itemContentType { "CurrencySelector" }
        ) { index ->
            currenciesPagingData[index]?.let { currency ->
                LabelledRadioButton(
                    label = "${currency.displayName} (${currency.currencyCode})",
                    selected = currency.currencyCode == selectedCode,
                    onClick = { onConfirm(currency) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                )
            }
        }
    }
}