package dev.ridill.rivo.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.delay

@Composable
fun SwipeToDismissContainer(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    animationDuration: Int = DEFAULT_ANIM_DURATION,
    enableDismissFromStartToEnd: Boolean = true,
    enableDismissFromEndToStart: Boolean = true,
    backgroundContent: @Composable RowScope.(SwipeToDismissBoxState) -> Unit = {},
    content: @Composable RowScope.() -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.Settled -> true
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (enableDismissFromStartToEnd) {
                        isRemoved = true
                        true
                    } else {
                        false
                    }
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    if (enableDismissFromEndToStart) {
                        isRemoved = true
                        true
                    } else {
                        false
                    }
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        state.snapTo(SwipeToDismissBoxValue.Settled)
    }

    LaunchedEffect(isRemoved) {
        if (isRemoved) {
            delay(animationDuration.toLong())
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = !isRemoved,
        exit = shrinkVertically(
            animationSpec = tween(durationMillis = animationDuration),
            shrinkTowards = Alignment.Top
        ) + fadeOut(),
        modifier = modifier
    ) {
        SwipeToDismissBox(
            state = state,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            backgroundContent = { backgroundContent(state) },
            content = content
        )
    }
}

private const val DEFAULT_ANIM_DURATION = 500

@Composable
fun DismissBackground(
    swipeDismissState: SwipeToDismissBoxState,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enableDismissFromStartToEnd: Boolean = true,
    enableDismissFromEndToStart: Boolean = true,
    contentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.errorContainer,
    contentColor: Color = contentColorFor(containerColor)
) {
    val color = if (
        (enableDismissFromStartToEnd && swipeDismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) ||
        (enableDismissFromEndToStart && swipeDismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
    ) containerColor
    else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (enableDismissFromStartToEnd) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor
            )
        }
        SpacerMedium()
        if (enableDismissFromEndToStart) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor
            )
        }
    }
}