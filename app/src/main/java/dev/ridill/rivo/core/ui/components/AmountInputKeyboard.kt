package dev.ridill.rivo.core.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import dev.ridill.rivo.core.ui.theme.RivoTheme
import dev.ridill.rivo.core.ui.theme.spacing

@Composable
fun AmountInputKeyboard(
    onKeyPress: (CalculatorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonSpacing = MaterialTheme.spacing.small

    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(buttonSpacing),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                items(
                    count = 9
                ) {
                    AmountInputKey(
                        key = (it + 1).toString(),
                        onClick = {},
                        modifier = Modifier
                            .aspectRatio(1f)
                    )
                }
            }

            Column {

            }
        }
    }
//    Column(
//        modifier = Modifier
//            .fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
//        ) {
//            AmountInputKey(
//                key = "AC",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(2f)
//                    .weight(2f),
//            )
//            AmountInputKey(
//                key = "Del",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//            AmountInputKey(
//                key = CalculatorOperation.DIVIDE.symbol,
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//        }
//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
//        ) {
//            AmountInputKey(
//                key = "7",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//            AmountInputKey(
//                key = "8",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//            AmountInputKey(
//                key = "9",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//            AmountInputKey(
//                key = CalculatorOperation.MULTIPLY.symbol,
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//        }
//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
//        ) {
//            AmountInputKey(
//                key = "4",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//            AmountInputKey(
//                key = "5",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//            AmountInputKey(
//                key = "6",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//            AmountInputKey(
//                key = CalculatorOperation.SUBTRACT.symbol,
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//        }
//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
//        ) {
//            AmountInputKey(
//                key = "1",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//            AmountInputKey(
//                key = "2",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//            AmountInputKey(
//                key = "3",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//            AmountInputKey(
//                key = CalculatorOperation.ADD.symbol,
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//        }
//        Row(
//            modifier = Modifier
//                .fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
//        ) {
//            AmountInputKey(
//                key = "0",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(2f)
//                    .weight(2f)
//            )
//            AmountInputKey(
//                key = ".",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//            AmountInputKey(
//                key = "=",
//                onClick = {},
//                modifier = Modifier
//                    .aspectRatio(1f)
//                    .weight(1f)
//            )
//        }
//    }
}

@Composable
private fun AmountInputKey(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = contentColorFor(containerColor)
) {
    val mutableInteractionSource = remember { MutableInteractionSource() }
    val isPressed by mutableInteractionSource.collectIsPressedAsState()
    val isHovered by mutableInteractionSource.collectIsHoveredAsState()
    val localDensity = LocalDensity.current
    val cornerRadiusPercent by animateIntAsState(
        targetValue = if (isHovered || isPressed) CORNER_RADIUS_PERCENT_HOVERED
        else CORNER_RADIUS_PERCENT_IDLE
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clickable(
                onClick = onClick,
                role = Role.Button,
                interactionSource = mutableInteractionSource,
                indication = LocalIndication.current
            )
            .drawBehind {
                drawRoundRect(
                    color = containerColor,
                    cornerRadius = CornerRadius(
                        x = CornerSize(percent = cornerRadiusPercent)
                            .toPx(size, localDensity),
                        y = CornerSize(percent = cornerRadiusPercent)
                            .toPx(size, localDensity)
                    )
                )
            }
            .then(modifier)
    ) {
        Text(
            text = key,
            fontSize = KeyTextSize,
            color = contentColor
        )
    }
}

private val KeyTextSize = 36.sp
private const val CORNER_RADIUS_PERCENT_HOVERED = 50
private const val CORNER_RADIUS_PERCENT_IDLE = 25

sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    data class Operation(val operation: CalculatorOperation) : CalculatorAction()
    object Calculate : CalculatorAction()
    object Decimal : CalculatorAction()
}

enum class CalculatorOperation(
    val symbol: String
) {
    ADD("+"),
    SUBTRACT("-"),
    MULTIPLY("x"),
    DIVIDE("/")
}

@Preview
@Composable
private fun PreviewAmountInputKeyboard() {
    RivoTheme {
        AmountInputKeyboard(
            onKeyPress = {}
        )
    }
}