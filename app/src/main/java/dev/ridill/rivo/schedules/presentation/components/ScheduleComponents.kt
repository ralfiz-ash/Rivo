package dev.ridill.rivo.schedules.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.WhiteSpace
import dev.ridill.rivo.core.ui.components.AmountWithTypeIndicator
import dev.ridill.rivo.core.ui.components.BodyMediumText
import dev.ridill.rivo.core.ui.components.ListItemLeadingContentContainer
import dev.ridill.rivo.core.ui.components.SpacerSmall
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.elevation
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

@Composable
fun ScheduleListItem(
    note: String?,
    amount: String,
    type: TransactionType,
    nextPaymentTimestamp: LocalDateTime?,
    lastPaymentTimestamp: LocalDateTime?,
    canMarkPaid: Boolean,
    onMarkPaidClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation
) {
    val nextPaymentDateFormatted = remember(nextPaymentTimestamp) {
        nextPaymentTimestamp?.format(DateUtil.Formatters.ddth_EEE_spaceSep)
            ?.replace(" ", "\n")
    }
    val scheduleItemContentDescription = stringResource(
        R.string.cd_schedule_of_amount_for_date,
        amount,
        nextPaymentDateFormatted.orEmpty()
    )

    Surface(
        color = colors.containerColor
    ) {
        Column {
            ListItem(
                headlineContent = {
                    val isNoteNullOrEmpty = remember(note) { note.isNullOrEmpty() }
                    Text(
                        text = note.orEmpty()
                            .ifEmpty { stringResource(R.string.generic_schedule_title) },
                        fontStyle = if (isNoteNullOrEmpty) FontStyle.Italic
                        else null,
                        color = LocalContentColor.current
                            .copy(
                                alpha = if (isNoteNullOrEmpty) ContentAlpha.SUB_CONTENT
                                else Float.One
                            )
                    )
                },
                leadingContent = nextPaymentDateFormatted?.let { date ->
                    {
                        ListItemLeadingContentContainer(
                            tonalElevation = MaterialTheme.elevation.level1
                        ) {
                            BodyMediumText(
                                text = nextPaymentDateFormatted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                trailingContent = {
                    AmountWithTypeIndicator(
                        value = amount,
                        type = type
                    )
                },
                supportingContent = lastPaymentTimestamp?.let { timestamp ->
                    {
                        Text(
                            text = stringResource(
                                R.string.last_payment_colon_value,
                                timestamp.format(DateUtil.Formatters.localizedDateMedium)
                            )
                        )
                    }
                },
                modifier = modifier
                    .semantics(mergeDescendants = true) {}
                    .clearAndSetSemantics {
                        contentDescription = scheduleItemContentDescription
                    },
                colors = colors,
                tonalElevation = tonalElevation,
                shadowElevation = shadowElevation,
            )

            if (canMarkPaid) {
                HorizontalDivider()
                SpacerSmall()
                TextButton(
                    onClick = onMarkPaidClick,
                    modifier = Modifier
                        .align(Alignment.End)
                ) {
                    Text(stringResource(R.string.mark_paid))
                }
            }
        }
    }
}

@Composable
fun ActiveScheduleItem(
    note: String?,
    amount: String,
    type: TransactionType,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(amount)
                }
                append(String.WhiteSpace)
                append(stringResource(type.labelRes))
                note?.let { text ->
                    append(String.WhiteSpace)
                    if (text.isNotEmpty()) {
                        append(stringResource(R.string.for_txt))
                        append(String.WhiteSpace)
                        withStyle(
                            SpanStyle(
                                fontStyle = FontStyle.Italic,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append(text)
                        }
                    }
                }
            },
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.small
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewScheduleListItemCard() {
    RivoTheme(
        darkTheme = true
    ) {
        ScheduleListItem(
            amount = "100",
            note = "Test",
            type = TransactionType.DEBIT,
            nextPaymentTimestamp = DateUtil.now(),
            lastPaymentTimestamp = DateUtil.now(),
            onMarkPaidClick = {},
            canMarkPaid = true,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}