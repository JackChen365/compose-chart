package me.jack.compose.chart.component

import androidx.annotation.FloatRange
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import me.jack.compose.chart.context.ChartScrollState
import me.jack.compose.chart.context.isElementAvailable
import me.jack.compose.chart.context.pressState
import me.jack.compose.chart.draw.DrawElement
import me.jack.compose.chart.draw.interaction.longPressTapState
import me.jack.compose.chart.draw.interaction.pressInteractionState
import me.jack.compose.chart.interaction.asPressInteraction
import me.jack.compose.chart.model.LineData
import me.jack.compose.chart.scope.ChartScope
import me.jack.compose.chart.scope.SingleChartScope
import me.jack.compose.chart.scope.isHorizontal


class MarkerSpec(
    val tooltipSize: DpSize = DpSize(width = 80.dp, height = 40.dp),
    val tooltipCornerRadius: CornerSize = CornerSize(8.dp),
    val tooltipTickSize: Dp = 12.dp,
    val borderColor: Color,
    val borderSize: Dp = 2.dp,
    val borderElevation: Dp = 8.dp,
)

@Composable
fun ChartScope.MarkerComponent(
    spec: MarkerSpec = MarkerSpec(borderColor = MaterialTheme.colorScheme.primary),
    leftTop: Offset,
    size: Size,
    displayInfo: String,
    focusPoint: Offset = Offset.Unspecified
) {
    if (!chartContext.longPressTapState.value) return
    val tooltipWidth = spec.tooltipSize.width.toPx()
    val tooltipHeight = spec.tooltipSize.height.toPx()
    val tooltipContentSize by remember {
        mutableStateOf(
            Size(
                width = tooltipWidth,
                height = tooltipHeight
            )
        )
    }
    var alignment = TooltipAlignment.Bottom
    val offset: IntOffset
    if (isHorizontal) {
        offset = if (Offset.Unspecified == focusPoint)
            IntOffset(
                x = (leftTop.x - tooltipContentSize.width / 2 + size.width / 2)
                    .coerceIn(0f, contentSize.width - tooltipContentSize.width)
                    .toInt(),
                y = (leftTop.y - tooltipContentSize.height - spec.tooltipTickSize.toPx())
                    .coerceAtLeast(0f)
                    .toInt()
            )
        else IntOffset(
            x = (focusPoint.x - tooltipContentSize.width / 2)
                .coerceIn(0f, contentSize.width - tooltipContentSize.width)
                .toInt(),
            y = (focusPoint.y - tooltipContentSize.height - spec.tooltipTickSize.toPx())
                .coerceAtLeast(0f)
                .toInt()
        )
    } else {
        offset = if (Offset.Unspecified == focusPoint)
            IntOffset(
                x = (size.width + spec.tooltipTickSize.toPx())
                    .coerceAtMost(contentSize.width - tooltipContentSize.width)
                    .toInt(),
                y = (leftTop.y - tooltipContentSize.height / 2 + size.height / 2)
                    .coerceIn(0f, contentSize.height - tooltipContentSize.height)
                    .toInt()
            )
        else IntOffset(
            x = (focusPoint.x + spec.tooltipTickSize.toPx())
                .coerceIn(0f, contentSize.width - tooltipContentSize.width)
                .toInt(),
            y = (leftTop.y - tooltipContentSize.height / 2 + size.height / 2)
                .coerceIn(0f, contentSize.height - tooltipContentSize.height)
                .toInt()
        )
        alignment = TooltipAlignment.Start
    }
    val shape = remember {
        TooltipShape(
            cornerRadius = spec.tooltipCornerRadius,
            alignment = alignment,
            tickSize = spec.tooltipTickSize
        )
    }
    Card(
        modifier = Modifier
            .width(spec.tooltipSize.width)
            .height(spec.tooltipSize.height)
            .offset { offset }
            .shadow(elevation = spec.borderElevation, shape = shape),
        shape = shape,
        border = BorderStroke(
            width = spec.borderSize,
            color = spec.borderColor
        )
    ) {
        Text(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentHeight(align = Alignment.CenterVertically)
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center,
            text = displayInfo
        )
    }
}

enum class TooltipAlignment {
    Top, Start, End, Bottom
}

class TooltipShape(
    private val cornerRadius: CornerSize = CornerSize(4.dp),
    private val alignment: TooltipAlignment = TooltipAlignment.Bottom,
    @FloatRange(from = 0.0, to = 1.0) private val offsetFraction: Float = 0.5f,
    private val tickSize: Dp = 12.dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerRadius = cornerRadius.toPx(size, density)
        val diameter = cornerRadius * 2
        val tickSize = with(density) { tickSize.toPx() }
        return Outline.Generic(
            Path().apply {
                reset()
                //Top left arc
                arcTo(
                    rect = Rect(
                        left = 0f,
                        top = 0f,
                        right = diameter,
                        bottom = diameter
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                if (alignment == TooltipAlignment.Top) {
                    // width minus both left and right round circle diameter and the tick size/2
                    val start = diameter + tickSize / 2 + (size.width - diameter * 2 - tickSize) * offsetFraction
                    lineTo(x = start - tickSize / 2, y = 0f)
                    lineTo(x = start, y = -tickSize)
                    lineTo(x = start + tickSize / 2, y = 0f)
                }
                lineTo(x = size.width - diameter, y = 0f)
                //Top right arc
                arcTo(
                    rect = Rect(
                        left = size.width - diameter,
                        top = 0f,
                        right = size.width,
                        bottom = diameter
                    ),
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                if (alignment == TooltipAlignment.End && layoutDirection == LayoutDirection.Ltr
                    || alignment == TooltipAlignment.Start && layoutDirection == LayoutDirection.Rtl
                ) {
                    val start = diameter + tickSize / 2 + (size.height - diameter * 2 - tickSize) * offsetFraction
                    lineTo(x = size.width, y = (start - tickSize * 0.5f))
                    lineTo(x = size.width + tickSize, y = start)
                    lineTo(x = size.width, y = (start + tickSize * 0.5f))
                }
                lineTo(x = size.width, y = size.height - diameter)
                // Bottom right arc
                arcTo(
                    rect = Rect(
                        left = size.width - 2 * cornerRadius,
                        top = size.height - 2 * cornerRadius,
                        right = size.width,
                        bottom = size.height
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = 90.0f,
                    forceMoveTo = false
                )
                if (alignment == TooltipAlignment.Bottom) {
                    val start = diameter + tickSize / 2 + (size.width - diameter * 2 - tickSize) * offsetFraction
                    lineTo(x = start + tickSize * 0.5f, y = size.height)
                    lineTo(x = start, y = size.height + tickSize)
                    lineTo(x = start - tickSize * 0.5f, y = size.height)
                }
                lineTo(x = 2 * cornerRadius, y = size.height)
                // Bottom left arc
                arcTo(
                    rect = Rect(
                        left = 0f,
                        top = size.height - 2 * cornerRadius,
                        right = 2 * cornerRadius,
                        bottom = size.height
                    ),
                    startAngleDegrees = 90.0f,
                    sweepAngleDegrees = 90.0f,
                    forceMoveTo = false
                )
                if (alignment == TooltipAlignment.Start && layoutDirection == LayoutDirection.Ltr
                    || alignment == TooltipAlignment.End && layoutDirection == LayoutDirection.Rtl
                ) {
                    val start = diameter + tickSize / 2 + (size.height - diameter * 2 - tickSize) * offsetFraction
                    lineTo(x = 0f, y = (start + tickSize * 0.5f))
                    lineTo(x = -tickSize, y = start)
                    lineTo(x = 0f, y = (start - tickSize * 0.5f))
                }
                lineTo(x = 0f, y = cornerRadius)
            }
        )
    }
}

@Composable
fun ChartScope.MarkerDashLineComponent(
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 2.dp,
    leftTop: Offset,
    contentSize: Size,
    focusPoint: Offset
) {
    val pathEffect = remember {
        PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    }
    Canvas(
        Modifier.fillMaxSize()
    ) {
        if (isHorizontal) {
            drawLine(
                color = color,
                start = Offset(
                    x = leftTop.x + contentSize.width / 2,
                    y = leftTop.y
                ),
                end = Offset(
                    x = leftTop.x + contentSize.width / 2,
                    y = leftTop.y + contentSize.height
                ),
                strokeWidth = strokeWidth.toPx(),
                pathEffect = pathEffect
            )
            drawLine(
                color = color,
                start = Offset(
                    x = leftTop.x,
                    y = if (focusPoint == Offset.Unspecified)
                        leftTop.y + contentSize.height / 2
                    else focusPoint.y
                ),
                end = Offset(
                    x = leftTop.x + contentSize.width,
                    y = if (focusPoint == Offset.Unspecified)
                        leftTop.y + contentSize.height / 2
                    else focusPoint.y
                ),
                strokeWidth = strokeWidth.toPx(),
                pathEffect = pathEffect
            )
        } else {
            drawLine(
                color = color,
                start = Offset(
                    x = leftTop.x,
                    y = leftTop.y + contentSize.height / 2
                ),
                end = Offset(
                    x = leftTop.x + contentSize.width,
                    y = leftTop.y + contentSize.height / 2
                ),
                strokeWidth = strokeWidth.toPx(),
                pathEffect = pathEffect
            )
            drawLine(
                color = color,
                start = Offset(
                    x = if (focusPoint == Offset.Unspecified)
                        leftTop.x + contentSize.width / 2
                    else focusPoint.x,
                    y = leftTop.y
                ),
                end = Offset(
                    x = if (focusPoint == Offset.Unspecified)
                        leftTop.x + contentSize.width / 2
                    else focusPoint.x,
                    y = leftTop.y + contentSize.height
                ),
                strokeWidth = strokeWidth.toPx(),
                pathEffect = pathEffect
            )
        }
    }
}


@Preview
@Composable
fun TooltipShapePreview() {
    MaterialTheme {
        Surface {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 24.dp, end = 24.dp)
                ) {
                    var sliderPosition by remember { mutableFloatStateOf(0f) }
                    val shape = TooltipShape(
                        offsetFraction = sliderPosition,
                    )
                    Card(
                        modifier = Modifier
                            .width(150.dp)
                            .height(60.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = shape
                            ),
                        shape = shape,
                        border = BorderStroke(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .align(Alignment.CenterHorizontally),
                            textAlign = TextAlign.Center,
                            text = "Hello world"
                        )
                    }
                    Slider(
                        value = sliderPosition,
                        onValueChange = { sliderPosition = it }
                    )
                    Text(text = sliderPosition.toString())
                }
            }
        }
    }
}