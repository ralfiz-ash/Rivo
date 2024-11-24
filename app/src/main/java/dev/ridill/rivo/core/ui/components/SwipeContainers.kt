package dev.ridill.rivo.core.ui.components

import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeGestures
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.theme.spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeToDismissContainer(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    animationDuration: Int = DEFAULT_ANIM_DURATION,
    gesturesEnabled: Boolean = true,
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
            gesturesEnabled = gesturesEnabled,
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
    contentColor: Color = contentColorFor(containerColor),
    contentInsets: WindowInsets = SwipeContainerDefaults.contentInsets
) {

    BottomSheetDefaults.windowInsets
    val color = if (
        (enableDismissFromStartToEnd && swipeDismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) ||
        (enableDismissFromEndToStart && swipeDismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
    ) containerColor
    else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawRect(color) }
            .padding(contentInsets.asPaddingValues())
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

@Composable
fun SwipeRevealContainer(
    isRevealed: Boolean,
    onRevealedChange: (Boolean) -> Unit,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    revealSide: RevealSide = RevealSide.End,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    gesturesEnabled: Boolean = true,
    @FloatRange(from = 0.0, to = 1.0) revealThreshold: Float = DEFAULT_REVEAL_THRESHOLD,
    revealAnimatable: Animatable<Float, AnimationVector1D> = remember { Animatable(initialValue = Float.Zero) },
    actionContentInsets: WindowInsets = SwipeContainerDefaults.contentInsets,
    content: @Composable () -> Unit
) {
    var contextMenuWidth by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isRevealed, contextMenuWidth, revealSide) {
        if (isRevealed) {
            revealAnimatable.animateTo(contextMenuWidth.times(revealSide.multiplier))
        } else {
            revealAnimatable.animateTo(Float.Zero)
        }
    }

    val actionsInsets = remember(revealSide) {
        when (revealSide) {
            RevealSide.Start -> actionContentInsets.only(WindowInsetsSides.Start)
            RevealSide.End -> actionContentInsets.only(WindowInsetsSides.End)
        }
    }

    val actionsAlignment = remember(revealSide) {
        when (revealSide) {
            RevealSide.Start -> Alignment.CenterStart
            RevealSide.End -> Alignment.CenterEnd
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(containerColor)
    ) {
        Row(
            modifier = Modifier
                .align(actionsAlignment)
                .onSizeChanged {
                    contextMenuWidth = it.width.toFloat()
                }
                .padding(actionsInsets.asPaddingValues())
                .then(
                    when (revealSide) {
                        RevealSide.Start -> Modifier.padding(end = MaterialTheme.spacing.small)
                        RevealSide.End -> Modifier.padding(start = MaterialTheme.spacing.small)
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
        Surface(
            content = content,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(revealAnimatable.value.roundToInt(), 0) }
                .pointerInput(contextMenuWidth, gesturesEnabled) {
                    if (gesturesEnabled) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    val newOffset = when (revealSide) {
                                        RevealSide.Start -> (revealAnimatable.value + dragAmount)
                                            .coerceIn(0f, contextMenuWidth)

                                        RevealSide.End -> (revealAnimatable.value + dragAmount)
                                            .coerceIn(-contextMenuWidth, 0f)
                                    }
                                    revealAnimatable.snapTo(newOffset)
                                }
                            },
                            onDragEnd = {
                                when {
                                    revealSide == RevealSide.Start && revealAnimatable.value >= (contextMenuWidth * revealThreshold) -> {
                                        scope.launch {
                                            revealAnimatable.animateTo(contextMenuWidth)
                                            onRevealedChange(true)
                                        }
                                    }

                                    revealSide == RevealSide.End && revealAnimatable.value <= -(contextMenuWidth * revealThreshold) -> {
                                        scope.launch {
                                            revealAnimatable.animateTo(-contextMenuWidth)
                                            onRevealedChange(true)
                                        }
                                    }

                                    else -> {
                                        scope.launch {
                                            revealAnimatable.animateTo(0f)
                                            onRevealedChange(false)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
        )
    }
}

private const val DEFAULT_REVEAL_THRESHOLD = 0.5f

enum class RevealSide(
    val multiplier: Int
) {
    Start(1),
    End(-1)
}

object SwipeContainerDefaults {

    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val contentInsets: WindowInsets
        @Composable get() = WindowInsets.safeGestures.only(WindowInsetsSides.Horizontal)
}