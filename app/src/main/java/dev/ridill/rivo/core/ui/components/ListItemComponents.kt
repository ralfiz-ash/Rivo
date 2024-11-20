package dev.ridill.rivo.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.core.ui.theme.elevation
import dev.ridill.rivo.core.ui.theme.spacing

@Composable
fun ListItemLeadingContentContainer(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = MaterialTheme.elevation.level0,
    contentPadding: PaddingValues = PaddingValues(MaterialTheme.spacing.small),
    content: @Composable BoxScope.() -> Unit
) {
    Surface(
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(min = ContainerMinWidth)
                .padding(contentPadding)
                .then(modifier),
            contentAlignment = Alignment.Center,
            content = content
        )
    }
}

private val ContainerMinWidth: Dp = 56.dp