package dev.ridill.rivo.schedules.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.NewLine
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.WhiteSpace
import dev.ridill.rivo.core.ui.components.AmountWithTypeIndicator
import dev.ridill.rivo.core.ui.components.BodyMediumText
import dev.ridill.rivo.core.ui.components.BodySmallText
import dev.ridill.rivo.core.ui.components.ListItemLeadingContentContainer
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.mergedContentDescription
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.presentation.components.TypeIndicatorIcon
import java.time.LocalDateTime

@Composable
fun ScheduleListItem(
    note: String?,
    amount: String,
    type: TransactionType,
    nextPaymentTimestamp: LocalDateTime?,
    lastPaymentTimestamp: LocalDateTime?,
    modifier: Modifier = Modifier,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation
) {
    val nextPaymentDateFormatted = remember(nextPaymentTimestamp) {
        nextPaymentTimestamp?.format(DateUtil.Formatters.MMM_ddth_spaceSep)
            ?.replace(String.WhiteSpace, String.NewLine)
    }
    val scheduleItemContentDescription = stringResource(
        R.string.cd_schedule_of_amount_for_date,
        amount,
        nextPaymentDateFormatted.orEmpty()
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
        leadingContent = {
            ListItemLeadingContentContainer {
                if (!nextPaymentDateFormatted.isNullOrEmpty()) {
                    BodyMediumText(
                        text = nextPaymentDateFormatted,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Icon(
                        imageVector = ImageVector.vectorResource(R.drawable.ic_outline_wallet_done),
                        contentDescription = null
                    )
                }
            }
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
            .mergedContentDescription(scheduleItemContentDescription),
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    )
}

@Composable
fun ActiveScheduleItem(
    note: String?,
    amount: String,
    type: TransactionType,
    paymentDay: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .widthIn(max = ActiveScheduleMaxWidth)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 30.dp, minHeight = 30.dp)
                    .height(IntrinsicSize.Max)
                    .aspectRatio(
                        ratio = Float.One,
                        matchHeightConstraintsFirst = true
                    )
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                TypeIndicatorIcon(type)
            }

            Column(
                modifier = Modifier
                    .weight(Float.One)
            ) {
                BodyMediumText(
                    text = note.orEmpty()
                        .ifEmpty { stringResource(type.labelRes) },
                    color = LocalContentColor.current.copy(
                        alpha = if (note.isNullOrEmpty()) ContentAlpha.SUB_CONTENT
                        else Float.One
                    ),
                    fontStyle = if (note.isNullOrEmpty()) FontStyle.Italic
                    else null
                )
                BodySmallText(
                    text = stringResource(R.string.due_on_date, paymentDay),
                    color = LocalContentColor.current.copy(alpha = ContentAlpha.SUB_CONTENT)
                )
            }

            AmountWithTypeIndicator(
                value = amount,
                type = type,
                showTypeIndicator = false
            )
        }
    }
}

private val ActiveScheduleMaxWidth = 300.dp

@PreviewLightDark
@Composable
private fun PreviewScheduleListItemCard() {
    RivoTheme {
        ScheduleListItem(
            amount = "100",
            note = "Test",
            type = TransactionType.DEBIT,
            nextPaymentTimestamp = DateUtil.now(),
            lastPaymentTimestamp = DateUtil.now(),
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@PreviewLightDark
@Composable
private fun PreviewActiveScheduleCard() {
    RivoTheme {
        ActiveScheduleItem(
            note = "Test",
            amount = "Rs.100",
            type = TransactionType.DEBIT,
            paymentDay = "10th Wed",
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}