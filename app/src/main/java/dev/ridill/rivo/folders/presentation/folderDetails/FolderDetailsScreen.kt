package dev.ridill.rivo.folders.presentation.folderDetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.paging.compose.LazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.AmountWithTypeIndicator
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.DismissBackground
import dev.ridill.rivo.core.ui.components.ExcludedIcon
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.ListSeparator
import dev.ridill.rivo.core.ui.components.MultiActionConfirmationDialog
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerExtraSmall
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.components.SwipeToDismissContainer
import dev.ridill.rivo.core.ui.components.TitleLargeText
import dev.ridill.rivo.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.rivo.core.ui.components.icons.CalendarClock
import dev.ridill.rivo.core.ui.components.listEmptyIndicator
import dev.ridill.rivo.core.ui.navigation.destinations.FolderDetailsScreenSpec
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.folders.domain.model.AggregateType
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import kotlin.math.absoluteValue

@Composable
fun FolderDetailsScreen(
    snackbarController: SnackbarController,
    state: FolderDetailsState,
    transactionPagingItems: LazyPagingItems<TransactionListItemUIModel>,
    actions: FolderDetailsActions,
    navigateToEditFolder: () -> Unit,
    navigateToAddEditTransaction: (Long?) -> Unit,
    navigateUp: () -> Unit
) {
    val areTransactionsEmpty by remember {
        derivedStateOf { transactionPagingItems.isEmpty() }
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    RivoScaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(FolderDetailsScreenSpec.labelRes)) },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                actions = {
                    IconButton(onClick = navigateToEditFolder) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.cd_edit_folder)
                        )
                    }

                    IconButton(onClick = actions::onDeleteClick) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteForever,
                            contentDescription = stringResource(R.string.cd_delete_folder)
                        )
                    }
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        snackbarController = snackbarController,
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .imePadding()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                top = MaterialTheme.spacing.medium,
                bottom = PaddingScrollEnd
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            item(
                key = "FolderDetails",
                contentType = "FolderDetails"
            ) {
                FolderDetails(
                    folderName = state.folderName,
                    isExcluded = state.isExcluded,
                    aggregateAmount = state.aggregateAmount,
                    aggregateType = state.aggregateType,
                    createdTimestamp = state.createdTimestampFormatted,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .animateItem()
                )
            }

            stickyHeader(
                key = "TransactionListHeader",
                contentType = "TransactionListHeader"
            ) {
                TransactionListHeader(
                    onNewTransactionClick = { navigateToAddEditTransaction(null) },
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .animateItem()
                )
            }

            listEmptyIndicator(
                isListEmpty = areTransactionsEmpty,
                messageRes = R.string.transactions_in_folder_list_empty_message
            )

            repeat(transactionPagingItems.itemCount) { index ->
                transactionPagingItems[index]?.let { item ->
                    when (item) {
                        is TransactionListItemUIModel.DateSeparator -> {
                            stickyHeader(
                                key = item.date.toString(),
                                contentType = "TransactionDateSeparator"
                            ) {
                                ListSeparator(
                                    label = item.date.format(DateUtil.Formatters.MMMM_yyyy_spaceSep),
                                    modifier = Modifier
                                        .animateItem()
                                )
                            }
                        }

                        is TransactionListItemUIModel.TransactionItem -> {
                            item(
                                key = item.transaction.id,
                                contentType = "TransactionListItem"
                            ) {
                                SwipeToDismissContainer(
                                    item = item.transaction,
                                    onDismiss = actions::onTransactionSwipeToDismiss,
                                    backgroundContent = {
                                        DismissBackground(
                                            swipeDismissState = it,
                                            icon = ImageVector.vectorResource(R.drawable.ic_outline_remove_folder),
                                            contentDescription = stringResource(R.string.cd_remove_from_folder),
                                            enableDismissFromEndToStart = false,
                                            modifier = Modifier
                                                .padding(horizontal = MaterialTheme.spacing.large)
                                        )
                                    },
                                    enableDismissFromEndToStart = false,
                                    modifier = Modifier
                                        .animateItem()
                                ) {
                                    Card(
                                        onClick = { navigateToAddEditTransaction(item.transaction.id) }
                                    ) {
                                        TransactionListItem(
                                            note = item.transaction.note,
                                            amount = item.transaction.amountFormatted,
                                            date = item.transaction.date,
                                            type = item.transaction.type,
                                            tag = item.transaction.tag,
                                            excluded = item.transaction.excluded
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.showDeleteConfirmation) {
            if (transactionPagingItems.itemCount == 0) {
                ConfirmationDialog(
                    titleRes = R.string.delete_folder_confirmation_title,
                    contentRes = R.string.action_irreversible_message,
                    onConfirm = actions::onDeleteFolderOnlyClick,
                    onDismiss = actions::onDeleteDismiss
                )
            } else {
                MultiActionConfirmationDialog(
                    title = stringResource(R.string.delete_folder_confirmation_title),
                    text = stringResource(R.string.action_irreversible_message),
                    primaryActionLabelRes = R.string.delete_folder,
                    additionalNote = stringResource(R.string.delete_folder_confirmation_note),
                    onPrimaryActionClick = actions::onDeleteFolderOnlyClick,
                    secondaryActionLabelRes = R.string.delete_folder_and_transactions,
                    onSecondaryActionClick = actions::onDeleteFolderAndTransactionsClick,
                    onDismiss = actions::onDeleteDismiss
                )
            }
        }
    }
}

@Composable
private fun FolderDetails(
    folderName: String,
    isExcluded: Boolean,
    aggregateAmount: Double,
    aggregateType: AggregateType,
    createdTimestamp: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = MaterialTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            AnimatedVisibility(isExcluded) {
                ExcludedIcon()
            }
            TitleLargeText(folderName)
        }

        AggregateAmountAndCreatedDate(
            aggregateAmount = aggregateAmount,
            aggregateType = aggregateType,
            date = createdTimestamp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        )

        HorizontalDivider()
    }
}

@Composable
private fun AggregateAmountAndCreatedDate(
    aggregateAmount: Double,
    aggregateType: AggregateType,
    date: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        AggregateAmount(
            amount = aggregateAmount,
            type = aggregateType,
            modifier = Modifier
                .weight(weight = Float.One, fill = false)
        )
        SpacerSmall()
        FolderCreatedDate(
            date = date
        )
    }
}

@Composable
private fun FolderCreatedDate(
    date: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(R.string.created),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        SpacerSmall()
        Icon(
            imageVector = Icons.Outlined.CalendarClock,
            contentDescription = stringResource(R.string.cd_folder_created_date)
        )
    }
}

@Composable
private fun TransactionListHeader(
    onNewTransactionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListLabel(stringResource(R.string.transactions))
        FilledTonalIconButton(onClick = onNewTransactionClick) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.cd_new_transaction_fab)
            )
        }
    }
}

@Composable
private fun AggregateAmount(
    amount: Double,
    type: AggregateType,
    modifier: Modifier = Modifier
) {
    val aggregateAmountContentDescription = when (type) {
        AggregateType.BALANCED -> stringResource(R.string.cd_folder_aggregate_amount_balanced)
        else -> stringResource(
            R.string.cd_folder_aggregate_amount_unbalanced,
            TextFormat.currencyAmount(amount),
            stringResource(type.labelRes)
        )
    }
    Row(
        modifier = modifier
            .mergedContentDescription(aggregateAmountContentDescription)
    ) {
        VerticalNumberSpinnerContent(
            number = amount,
            modifier = Modifier
                .weight(weight = Float.One, fill = false)
                .alignBy(LastBaseline)
        ) {
            AmountWithTypeIndicator(
                value = TextFormat.currencyAmount(it.absoluteValue),
                type = type
            )
        }

        SpacerExtraSmall()

        Crossfade(
            targetState = type.labelRes,
            label = "AggregateType",
            modifier = Modifier
                .alignBy(LastBaseline)
        ) { resId ->
            Text(
                text = stringResource(resId),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}