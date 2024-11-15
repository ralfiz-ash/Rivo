package dev.ridill.rivo.schedules.presentation.components

import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.ui.components.AmountWithTypeIndicator
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.transactions.domain.model.TransactionType

@Composable
fun ScheduleListItem(
    note: String?,
    amount: String,
    type: TransactionType,
    nextReminderTimestamp: String?,
    lastPaymentTimestamp: String?,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation
) {
    val scheduleItemContentDescription = stringResource(
        R.string.cd_schedule_of_amount_for_date,
        amount,
        nextReminderTimestamp.orEmpty()
    )

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
        trailingContent = {
            AmountWithTypeIndicator(
                value = amount,
                type = type
            )
        },
        supportingContent = nextReminderTimestamp?.let { nextReminder ->
            {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.next_payment_on_colon))
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append(nextReminder)
                        }
                    }
                )
            }
        },
        overlineContent = lastPaymentTimestamp?.let { lastPayment ->
            {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.last_payment_colon))
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.secondary)) {
                            append(lastPayment)
                        }
                    }
                )
            }
        },
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                contentDescription = scheduleItemContentDescription
            }
            .then(modifier),
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    )
}