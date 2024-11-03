package dev.ridill.rivo.core.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.theme.PaddingScrollEnd
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.UiText

@Composable
fun RivoModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = BottomSheetDefaults.Elevation,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,
        properties = properties,
        content = content
    )
}

@Composable
fun OutlinedTextFieldSheet(
    @StringRes titleRes: Int,
    inputValue: () -> String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    text: String? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    singleLine: Boolean = true,
    placeholder: String? = null,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    @StringRes actionLabel: Int = R.string.action_confirm,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) = OutlinedTextFieldSheet(
    title = {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.medium)
        )
    },
    inputValue = inputValue,
    onValueChange = onValueChange,
    onDismiss = onDismiss,
    actionButton = {
        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Text(stringResource(actionLabel))
        }
    },
    modifier = modifier,
    text = text?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
        }
    },
    focusRequester = focusRequester,
    errorMessage = errorMessage,
    singleLine = singleLine,
    placeholder = placeholder,
    label = label,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    visualTransformation = visualTransformation,
    prefix = prefix,
    suffix = suffix,
    textStyle = textStyle,
    contentAfterTextField = contentAfterTextField,
    textFieldModifier = textFieldModifier
)

@Composable
fun OutlinedTextFieldSheet(
    title: @Composable () -> Unit,
    inputValue: () -> String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    actionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    textFieldModifier: Modifier = Modifier,
    text: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    singleLine: Boolean = true,
    placeholder: String? = null,
    label: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) {
    val isInputEmpty by remember {
        derivedStateOf { inputValue().isEmpty() }
    }
    RivoModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            modifier = Modifier
        ) {
            title()

            text?.invoke()

            OutlinedTextField(
                value = inputValue(),
                onValueChange = onValueChange,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .focusRequester(focusRequester)
                    .then(textFieldModifier),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                label = label?.let { { Text(it) } },
                supportingText = { errorMessage?.let { Text(it.asString()) } },
                isError = errorMessage != null,
                placeholder = placeholder?.let { { Text(it) } },
                singleLine = singleLine,
                trailingIcon = {
                    if (!isInputEmpty) {
                        IconButton(onClick = { onValueChange(String.Empty) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_clear)
                            )
                        }
                    }
                },
                visualTransformation = visualTransformation,
                prefix = prefix,
                suffix = suffix,
                textStyle = textStyle
            )

            contentAfterTextField?.invoke(this)

            Box(
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                actionButton()
            }
        }
    }
}

@Composable
fun TextFieldSheet(
    title: @Composable () -> Unit,
    inputValue: () -> String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    actionButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    text: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = LocalTextStyle.current,
    focusRequester: FocusRequester = remember { FocusRequester() },
    errorMessage: UiText? = null,
    singleLine: Boolean = true,
    placeholder: String? = null,
    label: String? = null,
    showClearOption: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    textFieldColors: TextFieldColors = TextFieldDefaults.colors(),
    contentAfterTextField: @Composable (ColumnScope.() -> Unit)? = null
) {
    val isInputEmpty by remember {
        derivedStateOf { inputValue().isEmpty() }
    }
    RivoModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            modifier = Modifier
                .padding(vertical = MaterialTheme.spacing.medium)
        ) {
            title()

            text?.invoke()

            TextField(
                value = inputValue(),
                onValueChange = onValueChange,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .focusRequester(focusRequester),
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                label = label?.let { { Text(it) } },
                supportingText = { errorMessage?.let { Text(it.asString()) } },
                isError = errorMessage != null,
                placeholder = placeholder?.let { { Text(it) } },
                singleLine = singleLine,
                trailingIcon = {
                    if (showClearOption && !isInputEmpty) {
                        IconButton(onClick = { onValueChange(String.Empty) }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.cd_clear)
                            )
                        }
                    }
                },
                visualTransformation = visualTransformation,
                prefix = prefix,
                suffix = suffix,
                textStyle = textStyle,
                colors = textFieldColors
            )

            contentAfterTextField?.invoke(this)

            Box(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(horizontal = MaterialTheme.spacing.medium)
            ) {
                actionButton()
            }
        }
    }
}

@Composable
fun ListSearchSheet(
    searchQuery: () -> String,
    onSearchQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    placeholder: String? = null,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(
        top = MaterialTheme.spacing.medium,
        bottom = PaddingScrollEnd
    ),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    additionalEndContent: @Composable (ColumnScope.() -> Unit)? = null,
    content: LazyListScope.() -> Unit
) {
    RivoModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = MaterialTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            if (title != null) {
                TitleLargeText(
                    text = title,
                    modifier = Modifier
                        .padding(horizontal = MaterialTheme.spacing.medium)
                )
            }
            SearchField(
                query = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                placeholder = placeholder,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
            )
            LazyColumn(
                state = listState,
                contentPadding = contentPadding,
                reverseLayout = reverseLayout,
                verticalArrangement = verticalArrangement,
                horizontalAlignment = horizontalAlignment,
                flingBehavior = flingBehavior,
                userScrollEnabled = userScrollEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(Float.One),
                content = content
            )

            additionalEndContent?.invoke(this)
        }
    }
}