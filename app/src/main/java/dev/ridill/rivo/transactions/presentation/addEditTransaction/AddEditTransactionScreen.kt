package dev.ridill.rivo.transactions.presentation.addEditTransaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.core.ui.components.AmountVisualTransformation
import dev.ridill.rivo.core.ui.components.BackArrowButton
import dev.ridill.rivo.core.ui.components.BodyMediumText
import dev.ridill.rivo.core.ui.components.ConfirmationDialog
import dev.ridill.rivo.core.ui.components.ExcludedIcon
import dev.ridill.rivo.core.ui.components.LabelledRadioButton
import dev.ridill.rivo.core.ui.components.MinWidthOutlinedTextField
import dev.ridill.rivo.core.ui.components.RivoDatePickerDialog
import dev.ridill.rivo.core.ui.components.RivoScaffold
import dev.ridill.rivo.core.ui.components.RivoTimePickerDialog
import dev.ridill.rivo.core.ui.components.SnackbarController
import dev.ridill.rivo.core.ui.components.SpacerExtraSmall
import dev.ridill.rivo.core.ui.components.SpacerMedium
import dev.ridill.rivo.core.ui.components.TitleLargeText
import dev.ridill.rivo.core.ui.components.icons.CalendarClock
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.navigation.destinations.AllSchedulesScreenSpec
import dev.ridill.rivo.core.ui.theme.IconSizeMedium
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.LocalCurrencyPreference
import dev.ridill.rivo.schedules.domain.model.ScheduleRepetition
import dev.ridill.rivo.settings.presentation.components.SimplePreference
import dev.ridill.rivo.settings.presentation.components.SwitchPreference
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.presentation.components.RecentTagsSelectorFlowRow
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.AmountRecommendationsRow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

@Composable
fun AddEditTransactionScreen(
    isEditMode: Boolean,
    snackbarController: SnackbarController,
    amountInput: () -> String,
    noteInput: () -> String,
    recentTagsLazyPagingItems: LazyPagingItems<Tag>,
    state: AddEditTransactionState,
    actions: AddEditTransactionActions,
    navigateUp: () -> Unit,
    navigateToAmountTransformation: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    val amountFocusRequester = remember { FocusRequester() }
    LaunchedEffect(isEditMode) {
        if (!isEditMode) {
            delay(500)
            amountFocusRequester.requestFocus()
        }
    }

    val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val dateNowUtc = remember { DateUtil.dateNow(ZoneId.of(ZoneOffset.UTC.id)) }
    val datePickerState = if (state.isScheduleTxMode) rememberDatePickerState(
        initialSelectedDateMillis = DateUtil.toMillis(state.timestampUtc),
        yearRange = IntRange(dateNowUtc.year, DatePickerDefaults.YearRange.last),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= DateUtil.toMillis(
                    date = dateNowUtc.plusDays(1),
                    zoneId = ZoneId.of(ZoneOffset.UTC.id)
                )
        }
    )
    else rememberDatePickerState(
        initialSelectedDateMillis = DateUtil.toMillis(state.timestampUtc),
        yearRange = IntRange(DatePickerDefaults.YearRange.first, dateNowUtc.year),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis < DateUtil.toMillis(
                    date = dateNowUtc.plusDays(1),
                    zoneId = ZoneId.of(ZoneOffset.UTC.id)
                )
        }
    )
    val timePickerState = rememberTimePickerState(
        initialHour = state.timestamp.hour,
        initialMinute = state.timestamp.minute
    )

    RivoScaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            id = if (isEditMode) {
                                if (state.isScheduleTxMode) R.string.edit_schedule
                                else R.string.destination_edit_transaction
                            } else {
                                if (state.isScheduleTxMode) R.string.new_schedule
                                else R.string.destination_new_transaction
                            }
                        )
                    )
                },
                navigationIcon = { BackArrowButton(onClick = navigateUp) },
                actions = {
                    OptionsMenu(
                        options = state.menuOptions,
                        onOptionClick = actions::onOptionClick
                    )
                },
                scrollBehavior = topAppBarScrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = actions::onSaveClick) {
                Icon(
                    imageVector = Icons.Rounded.Save,
                    contentDescription = stringResource(
                        id = if (state.isScheduleTxMode) R.string.cd_save_schedule
                        else R.string.cd_save_transaction
                    )
                )
            }
        },
        modifier = Modifier
            .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .imePadding(),
        snackbarController = snackbarController
    ) { paddingValues ->
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = MaterialTheme.spacing.medium,
                        bottom = PaddingScrollEnd
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
            ) {
                TransactionTypeSelector(
                    selectedType = state.transactionType,
                    onValueChange = actions::onTypeChange,
                    modifier = Modifier
                        .fillMaxWidth(TRANSACTION_DIRECTION_SELECTOR_WIDTH_FRACTION)
                        .align(Alignment.CenterHorizontally)
                )

                AmountInput(
                    amount = amountInput,
                    isInputAnExpression = state.isAmountInputAnExpression,
                    onAmountChange = actions::onAmountChange,
                    onExpressionEvalClick = actions::onEvaluateExpressionClick,
                    onTransformClick = navigateToAmountTransformation,
                    onFocusLost = actions::onAmountFocusLost,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .focusRequester(amountFocusRequester)
                )

                NoteInput(
                    input = noteInput,
                    onValueChange = actions::onNoteChange,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )

                if (!isEditMode) {
                    AmountRecommendationsRow(
                        recommendations = state.amountRecommendations,
                        onRecommendationClick = {
                            actions.onRecommendedAmountClick(it)
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                        modifier = Modifier
                            .fillMaxWidth(AMOUNT_RECOMMENDATION_WIDTH_FRACTION)
                    )
                }

                HorizontalDivider()

                TransactionTimestamp(
                    timestamp = state.timestamp,
                    onClick = actions::onTimestampClick,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .align(Alignment.End)
                )

                FolderIndicator(
                    folderName = state.linkedFolderName,
                    onSelectFolderClick = actions::onSelectFolderClick,
                    modifier = Modifier
                        .align(Alignment.Start)
                )

                AnimatedVisibility(
                    visible = state.isScheduleTxMode,
                    modifier = Modifier
                        .align(Alignment.Start)
                ) {
                    TransactionRepeatModeIndicator(
                        selectedRepeatMode = state.selectedRepetition,
                        onClick = actions::onRepeatModeClick,
                        modifier = Modifier
                            .padding(horizontal = MaterialTheme.spacing.medium)
                    )
                }
                HorizontalDivider()

                SwitchPreference(
                    titleRes = R.string.exclude_from_expenditure,
                    value = state.isTransactionExcluded,
                    onValueChange = actions::onExclusionToggle,
                    leadingIcon = { ExcludedIcon() }
                )

                HorizontalDivider()

                TagSelection(
                    tagsLazyPagingItems = recentTagsLazyPagingItems,
                    selectedTagId = state.selectedTagId,
                    onTagClick = actions::onTagSelect,
                    onViewAllClick = actions::onViewAllTagsClick,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )
            }

            AnimatedVisibility(
                visible = state.isLoading,
                modifier = Modifier
                    .align(Alignment.TopCenter)
            ) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }

        if (state.showDeleteConfirmation) {
            ConfirmationDialog(
                title = if (state.isScheduleTxMode) pluralStringResource(
                    R.plurals.delete_schedules_confirmation_title,
                    Int.One
                )
                else pluralStringResource(
                    R.plurals.delete_transactions_confirmation_title,
                    Int.One
                ),
                content = stringResource(R.string.action_irreversible_message),
                onConfirm = actions::onDeleteConfirm,
                onDismiss = actions::onDeleteDismiss
            )
        }

        if (state.showDatePicker) {
            RivoDatePickerDialog(
                onDismiss = actions::onDateSelectionDismiss,
                onConfirm = actions::onDateSelectionConfirm,
                onPickTimeClick = actions::onPickTimeClick,
                state = datePickerState
            )
        }

        if (state.showTimePicker) {
            RivoTimePickerDialog(
                onDismiss = actions::onTimeSelectionDismiss,
                onConfirm = actions::onTimeSelectionConfirm,
                onPickDateClick = actions::onPickDateClick,
                state = timePickerState
            )
        }

        if (state.showRepeatModeSelection) {
            RepetitionSelectionSheet(
                onDismiss = actions::onRepeatModeDismiss,
                selectedRepetition = state.selectedRepetition,
                onRepetitionSelect = actions::onRepetitionSelect,
            )
        }
    }
}

@Composable
private fun AmountInput(
    amount: () -> String,
    isInputAnExpression: Boolean,
    onAmountChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    onExpressionEvalClick: () -> Unit,
    onTransformClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amountContentDescription = stringResource(R.string.cd_enter_amount)
    val showTransformButton by remember {
        derivedStateOf { amount().toDoubleOrNull().orZero() > Double.Zero }
    }
    val focusManager = LocalFocusManager.current
    MinWidthOutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        modifier = modifier
            .onFocusChanged { focusState ->
                if (!focusState.isFocused) onFocusLost()
            }
            .semantics {
                contentDescription = amountContentDescription
            },
        prefix = { Text(LocalCurrencyPreference.current.symbol) },
        textStyle = MaterialTheme.typography.headlineMedium,
        placeholder = {
            Text(
                text = stringResource(R.string.amount_zero),
                style = MaterialTheme.typography.headlineMedium
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent
        ),
        visualTransformation = remember { AmountVisualTransformation() },
        trailingIcon = {
            when {
                isInputAnExpression -> {
                    IconButton(onClick = onExpressionEvalClick) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_equals),
                            contentDescription = stringResource(R.string.cd_evaluate_expression),
                            modifier = Modifier
                                .size(IconSizeMedium)
                        )
                    }
                }

                showTransformButton -> {
                    IconButton(onClick = onTransformClick) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.ic_rounded_gears),
                            contentDescription = stringResource(R.string.cd_transform_amount)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun NoteInput(
    input: () -> String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = input(),
        onValueChange = onValueChange,
        modifier = modifier
            .defaultMinSize(minWidth = NoteMinWidth),
        placeholder = {
            Text(
                text = stringResource(R.string.add_a_note),
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current,
                modifier = Modifier
                    .defaultMinSize(minWidth = NoteMinWidth),
            )
        },
        shape = MaterialTheme.shapes.medium,
        singleLine = false,
        maxLines = NOTE_MAX_LINES,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent
        ),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Default
        ),
        textStyle = LocalTextStyle.current.copy(
            textAlign = TextAlign.Center
        )
    )
}

private const val NOTE_MAX_LINES = 5
private val NoteMinWidth = 160.dp

private const val TRANSACTION_DIRECTION_SELECTOR_WIDTH_FRACTION = 0.80f
private const val AMOUNT_RECOMMENDATION_WIDTH_FRACTION = 0.80f

@Composable
private fun TransactionTimestamp(
    timestamp: LocalDateTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = stringResource(R.string.timestamp_label),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = timestamp.format(DateUtil.Formatters.localizedDateMediumTimeShort),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        FilledTonalIconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Outlined.CalendarClock,
                contentDescription = stringResource(R.string.cd_tap_to_pick_timestamp)
            )
        }
    }
}

@Composable
private fun FolderIndicator(
    folderName: String?,
    onSelectFolderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SimplePreference(
        title = folderName ?: stringResource(R.string.transaction_folder_indicator_label),
        leadingIcon = {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outline_move_to_folder),
                contentDescription = stringResource(R.string.cd_add_transaction_to_folder)
            )
        },
        onClick = onSelectFolderClick,
        modifier = modifier
            .fillMaxWidth(),
        summary = stringResource(R.string.tap_to_select_folder)
    )
}

@Composable
private fun TransactionTypeSelector(
    selectedType: TransactionType,
    onValueChange: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val typeSelectorContentDescription = stringResource(
        R.string.cd_transaction_type_selector,
        stringResource(selectedType.labelRes)
    )

    val typesCount = remember { TransactionType.entries.size }

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .semantics {
                contentDescription = typeSelectorContentDescription
            },
    ) {
        TransactionType.entries.forEachIndexed { index, type ->
            val selected = selectedType == type
            val transactionTypeSelectorContentDescription = if (!selected)
                stringResource(
                    R.string.cd_transaction_type_selector_unselected,
                    stringResource(type.labelRes)
                )
            else null
            SegmentedButton(
                selected = selected,
                onClick = { onValueChange(type) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = typesCount),
                modifier = Modifier
                    .semantics {
                        transactionTypeSelectorContentDescription?.let {
                            contentDescription = it
                        }
                    },
                label = { Text(stringResource(type.labelRes)) },
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(type.iconRes),
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Composable
private fun TagSelection(
    tagsLazyPagingItems: LazyPagingItems<Tag>,
    selectedTagId: Long?,
    onTagClick: (Long) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
    ) {
        BodyMediumText(stringResource(R.string.tag_your_transaction))
        RecentTagsSelectorFlowRow(
            recentTagsLazyPagingItems = tagsLazyPagingItems,
            selectedTagId = selectedTagId,
            onTagClick = onTagClick,
            onViewAllClick = onViewAllClick,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun TransactionRepeatModeIndicator(
    selectedRepeatMode: ScheduleRepetition,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        ElevatedAssistChip(
            onClick = onClick,
            label = {
                Text(
                    text = stringResource(
                        R.string.repeat_mode_label_schedule,
                        stringResource(selectedRepeatMode.labelRes)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_outline_repeat_duration),
                    contentDescription = stringResource(R.string.cd_transaction_repeat_mode)
                )
            }
        )

        SpacerExtraSmall()

        Text(
            text = stringResource(
                R.string.scheduled_transactions_can_be_found_in_corresponding_screen,
                stringResource(AllSchedulesScreenSpec.labelRes)
            ),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .widthIn(max = 240.dp)
        )
    }
}

@Composable
private fun RepetitionSelectionSheet(
    onDismiss: () -> Unit,
    selectedRepetition: ScheduleRepetition,
    onRepetitionSelect: (ScheduleRepetition) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(true)
    ) {
        TitleLargeText(
            text = stringResource(R.string.select_schedule_repetition),
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.medium)
        )
        SpacerMedium()
        ScheduleRepetition.entries.forEach { repetition ->
            LabelledRadioButton(
                labelRes = repetition.labelRes,
                selected = repetition == selectedRepetition,
                onClick = { onRepetitionSelect(repetition) },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun OptionsMenu(
    options: Set<AddEditTxOption>,
    onOptionClick: (AddEditTxOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
    ) {
        IconButton(
            onClick = { actionsExpanded = !actionsExpanded }
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.cd_show_options)
            )
        }

        DropdownMenu(
            expanded = actionsExpanded,
            onDismissRequest = { actionsExpanded = false },
            shape = MaterialTheme.shapes.small
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.labelRes)) },
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(option.iconRes),
                            contentDescription = null
                        )
                    },
                    onClick = {
                        actionsExpanded = false
                        onOptionClick(option)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewScreenContent() {
    RivoTheme {
        AddEditTransactionScreen(
            isEditMode = false,
            snackbarController = rememberSnackbarController(),
            amountInput = { "" },
            noteInput = { "" },
            recentTagsLazyPagingItems = flowOf(PagingData.empty<Tag>()).collectAsLazyPagingItems(),
            state = AddEditTransactionState(
                isScheduleTxMode = true,
                selectedRepetition = ScheduleRepetition.MONTHLY
            ),
            actions = object : AddEditTransactionActions {
                override fun onAmountChange(value: String) {}
                override fun onAmountFocusLost() {}
                override fun onEvaluateExpressionClick() {}
                override fun onNoteChange(value: String) {}
                override fun onRecommendedAmountClick(amount: Long) {}
                override fun onTagSelect(tagId: Long) {}
                override fun onViewAllTagsClick() {}
                override fun onTimestampClick() {}
                override fun onDateSelectionDismiss() {}
                override fun onPickTimeClick() {}
                override fun onPickDateClick() {}
                override fun onDateSelectionConfirm(millis: Long) {}
                override fun onTimeSelectionDismiss() {}
                override fun onTimeSelectionConfirm(hour: Int, minute: Int) {}
                override fun onTypeChange(type: TransactionType) {}
                override fun onExclusionToggle(excluded: Boolean) {}
                override fun onDeleteDismiss() {}
                override fun onDeleteConfirm() {}
                override fun onSelectFolderClick() {}
                override fun onRepeatModeClick() {}
                override fun onOptionClick(option: AddEditTxOption) {}
                override fun onRepeatModeDismiss() {}
                override fun onRepetitionSelect(repetition: ScheduleRepetition) {}
                override fun onSaveClick() {}
            },
            navigateUp = {},
            navigateToAmountTransformation = {}
        )
    }
}