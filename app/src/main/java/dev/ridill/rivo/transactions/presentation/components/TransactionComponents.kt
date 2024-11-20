package dev.ridill.rivo.transactions.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.NewLine
import dev.ridill.rivo.core.domain.util.One
import dev.ridill.rivo.core.domain.util.WhiteSpace
import dev.ridill.rivo.core.ui.components.AmountWithTypeIndicator
import dev.ridill.rivo.core.ui.components.BodyMediumText
import dev.ridill.rivo.core.ui.components.ExcludedIndicatorSmall
import dev.ridill.rivo.core.ui.components.ListItemLeadingContentContainer
import dev.ridill.rivo.core.ui.components.icons.Tags
import dev.ridill.rivo.core.ui.theme.ContentAlpha
import dev.ridill.rivo.core.ui.theme.spacing
import dev.ridill.rivo.core.ui.util.exclusionGraphicsLayer
import dev.ridill.rivo.transactions.domain.model.FolderIndicator
import dev.ridill.rivo.transactions.domain.model.TagIndicator
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDate
import kotlin.text.ifEmpty

@Composable
fun TransactionListItem(
    note: String,
    amount: String,
    date: LocalDate,
    type: TransactionType,
    modifier: Modifier = Modifier,
    tag: TagIndicator? = null,
    folder: FolderIndicator? = null,
    excluded: Boolean = false,
    overlineContent: @Composable (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation
) {
    val dateMultiLined = remember(date) {
        date.format(DateUtil.Formatters.ddth_EEE_spaceSep)
            .replace(String.WhiteSpace, String.NewLine)
    }

    val transactionListItemContentDescription = buildString {
        append(
            stringResource(
                when (type) {
                    TransactionType.CREDIT -> R.string.cd_transaction_list_item_credit
                    TransactionType.DEBIT -> R.string.cd_transaction_list_item_debit
                },
                amount,
                note,
                date.format(DateUtil.Formatters.localizedDateLong)
            )
        )

        tag?.let {
            append(String.WhiteSpace)
            append(stringResource(R.string.cd_transaction_list_item_tag_append, it.name))
        }

        folder?.let {
            append(String.WhiteSpace)
            append(stringResource(R.string.cd_transaction_list_item_folder_append, it.name))
        }
    }
    ListItem(
        headlineContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                if (note.isEmpty() && (tag != null || folder != null)) {
                    TagAndFolderIndicator(
                        tag = tag,
                        folder = folder
                    )
                } else {
                    NoteText(
                        note = note,
                        type = type
                    )
                }
            }
        },
        leadingContent = {
            ListItemLeadingContentContainer {
                BodyMediumText(
                    text = dateMultiLined,
                    textAlign = TextAlign.Center
                )
            }
        },
        trailingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                if (excluded) {
                    ExcludedIndicatorSmall()
                }
                AmountWithTypeIndicator(
                    value = amount,
                    type = type
                )
            }
        },
        supportingContent = {
            if (note.isNotEmpty()) {
                TagAndFolderIndicator(
                    tag = tag,
                    folder = folder,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        },
        overlineContent = overlineContent,
        modifier = Modifier
            .semantics(mergeDescendants = true) {}
            .clearAndSetSemantics {
                contentDescription = transactionListItemContentDescription
            }
            .then(modifier)
            .exclusionGraphicsLayer(excluded),
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation
    )
}

@Composable
private fun NoteText(
    note: String,
    type: TransactionType,
    modifier: Modifier = Modifier
) {
    Text(
        text = note
            .ifEmpty { stringResource(type.labelRes) },
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
        color = LocalContentColor.current.copy(
            alpha = if (note.isEmpty()) ContentAlpha.SUB_CONTENT
            else Float.One
        ),
        style = LocalTextStyle.current.copy(
            fontStyle = if (note.isEmpty()) FontStyle.Italic
            else null
        )
    )
}

@Composable
private fun TagAndFolderIndicator(
    tag: TagIndicator?,
    folder: FolderIndicator?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier
    ) {
        tag?.let {
            TagIndicator(
                name = it.name,
                color = it.color,
                modifier = Modifier
                    .weight(weight = Float.One, fill = false)
            )
        }
        folder?.let {
            FolderIndicator(
                name = it.name,
                modifier = Modifier
                    .weight(weight = Float.One, fill = false)
            )
        }
    }
}

@Composable
private fun TagIndicator(
    name: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Icon(
            imageVector = Icons.Rounded.Tags,
            contentDescription = null,
            tint = color,
            modifier = Modifier
                .size(SmallIndicatorSize)
        )
        Text(
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FolderIndicator(
    name: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_filled_folder),
            contentDescription = null,
            modifier = Modifier
                .size(SmallIndicatorSize)
        )
        Text(
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val SmallIndicatorSize = 12.dp

@Composable
fun NewTransactionFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation()
) {
    FloatingActionButton(
        onClick = onClick,
        elevation = elevation,
        modifier = modifier
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_outline_money_add),
            contentDescription = stringResource(R.string.cd_new_transaction_fab)
        )
    }
}