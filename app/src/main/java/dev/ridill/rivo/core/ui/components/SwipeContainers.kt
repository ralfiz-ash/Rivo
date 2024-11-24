package dev.ridill.rivo.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.theme.spacing
import kotlinx.coroutines.delay
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
    state: AnchoredDraggableState<Boolean>,
    actions: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    revealSide: RevealSide = RevealSide.End,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    gesturesEnabled: Boolean = true,
    actionContentInsets: WindowInsets = SwipeContainerDefaults.contentInsets,
    content: @Composable () -> Unit
) {
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
                .onSizeChanged { measuresSize ->
                    state.updateAnchors(
                        newAnchors = DraggableAnchors {
                            false at Float.Zero
                            true at measuresSize.width
                                .toFloat()
                                .times(revealSide.multiplier)
                        }
                    )
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
                .offset {
                    IntOffset(
                        x = state
                            .requireOffset()
                            .roundToInt(),
                        y = 0
                    )
                }
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal,
                    enabled = gesturesEnabled
                )
        )
    }
}

enum class RevealSide(
    val multiplier: Int
) {
    Start(1),
    End(-1)
}

@Composable
fun rememberSwipeRevealState(
    positionalThreshold: Float = SwipeContainerDefaults.POSITIONAL_THRESHOLD_FRACTION,
    velocityThreshold: Float = SwipeContainerDefaults.velocityThreshold,
    snapAnimationSpec: AnimationSpec<Float> = SwipeContainerDefaults.snapAnimationSpec,
    decayAnimationSpec: DecayAnimationSpec<Float> = SwipeContainerDefaults.decayAnimationSpec
): AnchoredDraggableState<Boolean> = rememberSaveable(
    saver = AnchoredDraggableState.Saver(
        snapAnimationSpec = snapAnimationSpec,
        decayAnimationSpec = decayAnimationSpec,
        positionalThreshold = { it * positionalThreshold },
        velocityThreshold = { velocityThreshold }
    )
) {
    AnchoredDraggableState(
        initialValue = false,
        positionalThreshold = { it * positionalThreshold },
        velocityThreshold = { velocityThreshold },
        snapAnimationSpec = snapAnimationSpec,
        decayAnimationSpec = decayAnimationSpec
    )
}

object SwipeContainerDefaults {

    val containerColor: Color
        @Composable get() = MaterialTheme.colorScheme.surfaceVariant

    val contentInsets: WindowInsets
        @Composable get() = WindowInsets.safeGestures.only(WindowInsetsSides.Horizontal)

    val velocityThreshold: Float
        @Composable get() = with(LocalDensity.current) { 125.dp.toPx() }

    val snapAnimationSpec: AnimationSpec<Float>
        get() = spring()

    val decayAnimationSpec: DecayAnimationSpec<Float>
        @Composable get() = rememberSplineBasedDecay()

    const val POSITIONAL_THRESHOLD_FRACTION = 0.5f
}