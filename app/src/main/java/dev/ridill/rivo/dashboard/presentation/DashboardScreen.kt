package dev.ridill.rivo.dashboard.presentation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.LastBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.PartOfDay
import dev.ridill.rivo.core.ui.components.ListLabel
import dev.ridill.rivo.core.ui.components.OnLifecycleStartEffect
import dev.ridill.rivo.core.ui.components.RivoPlainTooltip
import dev.ridill.rivo.core.ui.components.RivoRichTooltip
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerExtraSmall
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.components.VerticalNumberSpinnerContent
import dev.ridill.rivo.core.ui.components.listEmptyIndicator
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.AllTransactionsScreenSpec
import dev.ridill.rivo.core.ui.navigation.destinations.BottomNavDestination
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.core.ui.util.isEmpty
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.schedules.domain.model.ActiveSchedule
import dev.ridill.rivo.schedules.presentation.components.ActiveScheduleItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.NewTransactionFab
import dev.ridill.rivo.transactions.presentation.components.TransactionListItem
import kotlinx.coroutines.flow.flowOf

@Composable
fun DashboardScreen(
    snackbarController: SnackbarController,
    recentSpends: LazyPagingItems<TransactionListItem>,
    state: DashboardState,
    onRecentSpendClick: () -> Unit,
    navigateToAllTransactions: () -> Unit,
    navigateToAddEditTransaction: (Long?) -> Unit,
    navigateToBottomNavDestination: (BottomNavDestination) -> Unit
) {
    val topAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val areActiveSchedulesEmpty by remember(state.activeSchedules) {
        derivedStateOf { state.activeSchedules.isEmpty() }
    }
    val areRecentSpendsEmpty by remember {
        derivedStateOf { recentSpends.isEmpty() }
    }

    RivoScaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Greeting(username = state.signedInUsername) },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    BottomNavDestination.bottomNavDestinations.forEach { destination ->
                        RivoPlainTooltip(
                            tooltipText = stringResource(destination.labelRes),
                            focusable = false
                        ) {
                            IconButton(
                                onClick = { navigateToBottomNavDestination(destination) },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(destination.iconRes),
                                    contentDescription = stringResource(destination.labelRes)
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    NewTransactionFab(
                        onClick = { navigateToAddEditTransaction(null) },
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    )
                }
            )
        },
        snackbarController = snackbarController
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                bottom = PaddingScrollEnd
            )
        ) {
            item(
                key = "BalanceAndBudget",
                contentType = "BalanceAndBudget"
            ) {
                BalanceAndBudget(
                    balance = state.balance,
                    budget = state.monthlyBudgetInclCredits,
                    creditAmount = state.creditAmount,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .animateItem()
                )
            }

            if (!areActiveSchedulesEmpty) {
                item(
                    key = "ActiveSchedulesRow",
                    contentType = "ActiveSchedulesRow"
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(vertical = MaterialTheme.spacing.small)
                            .animateItem(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(vertical = MaterialTheme.spacing.small)
                                .fillParentMaxWidth()
                        ) {
                            ListLabel(
                                text = stringResource(R.string.schedules_this_month),
                                modifier = Modifier
                                    .padding(horizontal = MaterialTheme.spacing.medium)
                            )

                            SpacerSmall()

                            HorizontalDivider(
                                modifier = Modifier
                                    .padding(horizontal = MaterialTheme.spacing.medium)
                            )

                            ActiveSchedulesRow(
                                activeSchedules = state.activeSchedules,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }

            stickyHeader(
                key = "RecentSpendsAmountOverview",
                contentType = "RecentSpendsAmountOverview"
            ) {
                Surface(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .animateItem()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.spacing.medium)
                            .padding(top = MaterialTheme.spacing.medium)
                    ) {
                        ListLabel(stringResource(R.string.recent_spends))

                        SpentAmountAndAllTransactionsButton(
                            showMarqueeTooltip = state.showRecentSpendMarqueeTooltip,
                            amount = state.spentAmount,
                            onRecentSpendClick = onRecentSpendClick,
                            onAllTransactionsClick = navigateToAllTransactions,
                            modifier = Modifier
                                .fillMaxWidth()
                        )

                        HorizontalDivider()
                    }
                }
            }

            listEmptyIndicator(
                isListEmpty = areRecentSpendsEmpty,
                messageRes = R.string.recent_spends_list_empty_message
            )

            items(
                count = recentSpends.itemCount,
                key = recentSpends.itemKey { it.id },
                contentType = recentSpends.itemContentType { "RecentSpendCard" }
            ) { index ->
                recentSpends[index]?.let { transaction ->
                    TransactionListItem(
                        note = transaction.note,
                        amount = transaction.amountFormatted,
                        date = transaction.date,
                        type = transaction.type,
                        tag = transaction.tag,
                        folder = transaction.folder,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .clickable(
                                onClick = { navigateToAddEditTransaction(transaction.id) },
                                onClickLabel = stringResource(R.string.cd_tap_to_edit_transaction)
                            )
                            .animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun Greeting(
    username: String?,
    modifier: Modifier = Modifier
) {
    var partOfDay by remember { mutableStateOf(PartOfDay.MORNING) }

    OnLifecycleStartEffect {
        partOfDay = DateUtil.getPartOfDay()
    }

    val isUsernameAvailable = remember(username) { !username.isNullOrEmpty() }

    Column(
        modifier = modifier
            .mergedContentDescription(
                contentDescription = stringResource(
                    R.string.cd_app_greeting_user,
                    stringResource(partOfDay.labelRes),
                    username.orEmpty()
                )
            )
    ) {
        Crossfade(targetState = partOfDay, label = "Greeting") { part ->
            Text(
                text = stringResource(R.string.app_greeting, stringResource(part.labelRes)),
                style = (if (isUsernameAvailable) MaterialTheme.typography.titleMedium
                else LocalTextStyle.current)
                    .copy(textMotion = TextMotion.Animated)
            )
        }
        username?.let { name ->
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                style = MaterialTheme.typography.titleLarge,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BalanceAndBudget(
    balance: Double,
    budget: Double,
    creditAmount: Double,
    modifier: Modifier = Modifier
) {
    val balanceAndBudgetContentDescription = stringResource(
        R.string.cd_balance_and_budget_amounts,
        TextFormat.currencyAmount(balance),
        TextFormat.currencyAmount(budget)
    )
    Row(
        modifier = modifier
            .mergedContentDescription(balanceAndBudgetContentDescription),
        verticalAlignment = Alignment.Bottom
    ) {
        Balance(
            amount = balance,
            modifier = Modifier
                .weight(weight = Float.One, fill = false)
                .alignBy(LastBaseline)
        )
        SpacerSmall()
        Box(
            modifier = Modifier
                .alignBy(LastBaseline)
        ) {
            RivoRichTooltip(
                tooltipTitle = stringResource(R.string.budget_includes_credited_amounts),
                tooltipText = stringResource(
                    R.string.budget_includes_credit_amount_of_value,
                    TextFormat.currencyAmount(creditAmount)
                ),
                state = rememberTooltipState(isPersistent = true)
            ) {
                Row {
                    VerticalNumberSpinnerContent(
                        number = budget,
                        modifier = Modifier
                            .alignBy(LastBaseline)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.fwd_slash_amount_value,
                                TextFormat.currencyAmount(amount = it)
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    SpacerExtraSmall()
                    Text(
                        text = stringResource(R.string.budget_asterisk),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .alignBy(LastBaseline)
                    )
                }
            }
        }
    }
}

@Composable
private fun Balance(
    amount: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.your_balance),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal
        )
        VerticalNumberSpinnerContent(number = amount) {
            Text(
                text = TextFormat.currencyAmount(amount = it),
                style = MaterialTheme.typography.displayLarge
                    .copy(lineBreak = LineBreak.Simple),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SpentAmountAndAllTransactionsButton(
    showMarqueeTooltip: Boolean,
    amount: Double,
    onRecentSpendClick: () -> Unit,
    onAllTransactionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amountMarqueeFocusRequester = remember { FocusRequester() }
    val contentColor = LocalContentColor.current
    val spentAmountContentDescription = stringResource(
        R.string.cd_recent_spent_amount,
        TextFormat.currencyAmount(amount)
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .weight(weight = Float.One, fill = false)
                .alignBy(LastBaseline)
                .mergedContentDescription(spentAmountContentDescription),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            VerticalNumberSpinnerContent(
                number = amount,
                modifier = Modifier
                    .weight(weight = Float.One, fill = false)
                    .alignBy(LastBaseline)
            ) {
                Text(
                    text = TextFormat.currencyAmount(amount = it),
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = contentColor,
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                onRecentSpendClick()
                                amountMarqueeFocusRequester.requestFocus()
                            },
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClickLabel = stringResource(R.string.cd_tap_to_view_full_amount)
                        )
                        .basicMarquee(
                            iterations = EXPENDITURE_MARQUEE_COUNT,
                            animationMode = MarqueeAnimationMode.WhileFocused
                        )
                        .focusRequester(amountMarqueeFocusRequester)
                        .focusable()
                )
            }

            Text(
                text = stringResource(R.string.spent_this_month),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier
                    .alignBy(LastBaseline),
                color = contentColor.copy(
                    alpha = ContentAlpha.SUB_CONTENT
                ),
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        TextButton(
            onClick = onAllTransactionsClick,
            modifier = Modifier
                .alignBy(LastBaseline)
        ) {
            Text("${stringResource(AllTransactionsScreenSpec.labelRes)} >")
        }
    }
}

private const val EXPENDITURE_MARQUEE_COUNT = 2

@Composable
private fun ActiveSchedulesRow(
    activeSchedules: List<ActiveSchedule>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(
            top = MaterialTheme.spacing.medium,
            start = MaterialTheme.spacing.medium,
            end = PaddingScrollEnd
        ),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        items(
            items = activeSchedules,
            key = { it.id },
            contentType = { "ActiveSchedule" }
        ) { schedule ->
            ActiveScheduleItem(
                note = schedule.note,
                amount = schedule.amountFormatted,
                type = schedule.type,
                paymentDay = schedule.dayFormatted,
                modifier = Modifier
                    .animateItem()
            )
        }
    }
}

@PreviewScreenSizes
@PreviewLightDark
@Composable
private fun PreviewDashboardScreen() {
    RivoTheme {
        DashboardScreen(
            state = DashboardState(
                balance = 1_000.0,
                spentAmount = 500.0,
                monthlyBudgetInclCredits = 5_000.0,
                activeSchedules = List(3) {
                    ActiveSchedule(
                        id = it.toLong(),
                        note = "Really long transaction note",
                        amount = 200.0,
                        currency = LocaleUtil.defaultCurrency,
                        type = TransactionType.DEBIT,
                        nextPaymentDateTime = DateUtil.now()
                    )
                }
            ),
            navigateToAllTransactions = {},
            navigateToAddEditTransaction = {},
            snackbarController = rememberSnackbarController(),
            navigateToBottomNavDestination = {},
            recentSpends = flowOf(PagingData.empty<TransactionListItem>()).collectAsLazyPagingItems(),
            onRecentSpendClick = {}
        )
    }
}