package me.jack.compose.chart.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.jack.compose.chart.context.ChartContext
import me.jack.compose.chart.context.chartInteraction
import me.jack.compose.chart.context.requireChartScrollState
import me.jack.compose.chart.context.scrollable
import me.jack.compose.chart.context.zoom
import me.jack.compose.chart.draw.ChartCanvas
import me.jack.compose.chart.draw.DrawElement
import me.jack.compose.chart.draw.interaction.pressInteractionState
import me.jack.compose.chart.interaction.asPressInteraction
import me.jack.compose.chart.measure.fixedContentMeasurePolicy
import me.jack.compose.chart.model.CandleData
import me.jack.compose.chart.model.asChartDataset
import me.jack.compose.chart.model.maxOf
import me.jack.compose.chart.model.minOf
import me.jack.compose.chart.scope.ChartAnchor
import me.jack.compose.chart.scope.SingleChartScope
import me.jack.compose.chart.scope.fastForEach
import kotlin.math.absoluteValue
import kotlin.math.min

class CandleStickSpec(
    val candleStickSize: Dp = 8.dp,
    val stackLineSize: Dp = 2.dp,
)

@Composable
fun SimpleCandleStickChart(
    modifier: Modifier = Modifier,
    chartData: List<CandleData>,
    candleStickSize: Dp = 32.dp,
    spec: CandleStickSpec = CandleStickSpec(),
    tapGestures: TapGestures<CandleData> = TapGestures(),
    content: @Composable SingleChartScope<CandleData>.() -> Unit = { ChartContent() }
) {
    CandleStickChart(
        modifier = modifier,
        chartData = chartData,
        candleStickSize = candleStickSize,
        spec = spec,
        tapGestures = tapGestures,
        content = content
    )
}

@Composable
fun CandleStickChart(
    modifier: Modifier = Modifier,
    chartData: List<CandleData>,
    candleStickSize: Dp = 32.dp,
    spec: CandleStickSpec = CandleStickSpec(),
    tapGestures: TapGestures<CandleData> = TapGestures(),
    content: @Composable SingleChartScope<CandleData>.() -> Unit = {
        CandleStickLeftSideLabel()
        ChartGridDividerComponent()
        ChartIndicatorComponent()
        ChartBorderComponent()
        ChartContent()
    }
) {
    val contentMeasurePolicy = fixedContentMeasurePolicy(candleStickSize.toPx())
    SingleChartLayout(
        modifier = modifier,
        chartDataset = chartData.asChartDataset(),
        tapGestures = tapGestures,
        contentMeasurePolicy = contentMeasurePolicy,
        chartContext = ChartContext
            .chartInteraction(
                remember { MutableInteractionSource() }
            ).scrollable(
                orientation = contentMeasurePolicy.orientation,
                state = rememberScrollState()
            ).zoom(),
        content = content,
        chartContent = {
            CandleStickComponent(spec)
            CandleDataMarkerComponent()
        }
    )
}

@Composable
fun SingleChartScope<CandleData>.CandleStickComponent(
    spec: CandleStickSpec = CandleStickSpec()
) {
    val scrollState = chartContext.requireChartScrollState
    val candleStickWidth = spec.candleStickSize.toPx()
    val stickLineWidth = spec.stackLineSize.toPx()
    ChartCanvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val highestValue = chartDataset.maxOf { it.high }
        val candleBlockSize = size.height / highestValue
        var offset = -scrollState.firstVisibleItemOffset
        // we calculate the lastVisibleItem due to other places need it.
        fastForEach { candleData ->
            clickableRect(
                topLeft = Offset(
                    x = offset,
                    y = 0f
                ),
                size = Size(width = childSize.mainAxis, height = size.height),
                focusPoint = Offset(
                    x = offset + childSize.mainAxis / 2,
                    y = candleBlockSize * (candleData.open + candleData.close) / 2
                )
            )
            drawRect(
                color = pressedColor.copy(alpha = 0f) whenPressedAnimateTo pressedColor,
                topLeft = Offset(
                    x = offset,
                    y = 0f
                ),
                size = Size(width = childSize.mainAxis, height = size.height)
            )
            drawRect(
                color = Color.Blue whenPressedAnimateTo Color.Blue.copy(alpha = 0.4f),
                topLeft = Offset(
                    x = offset + (childSize.mainAxis - stickLineWidth) / 2,
                    y = candleBlockSize * candleData.low
                ),
                size = Size(
                    width = stickLineWidth,
                    height = candleBlockSize * (candleData.high - candleData.low)
                )
            )
            val color = if (candleData.open > candleData.close) Color.Red else Color.Green
            drawRect(
                color = color whenPressedAnimateTo color.copy(alpha = 0.4f),
                topLeft = Offset(
                    x = offset + (childSize.mainAxis - candleStickWidth) / 2,
                    y = candleBlockSize * min(candleData.open, candleData.close)
                ),
                size = Size(
                    width = candleStickWidth,
                    height = candleBlockSize * (candleData.open - candleData.close).absoluteValue
                )
            )
            offset += childSize.mainAxis
        }
    }
}

@Composable
fun SingleChartScope<CandleData>.CandleDataMarkerComponent() {
    val pressInteraction = chartContext.pressInteractionState.value.asPressInteraction<CandleData>() ?: return
    val currentItem = pressInteraction.currentItem
    val drawElement = pressInteraction.drawElement
    if (drawElement is DrawElement.Rect) {
        MarkerDashLineComponent(
            leftTop = drawElement.topLeft,
            contentSize = drawElement.size,
            focusPoint = drawElement.focusPoint
        )
        MarkerComponent(
            leftTop = drawElement.topLeft,
            size = drawElement.size,
            focusPoint = drawElement.focusPoint,
            displayInfo = "(" + currentItem.high + "-" + currentItem.low + ")" +
                    "(" + currentItem.open + "-" + currentItem.close + ")"
        )
    }
}

@Composable
fun SingleChartScope<CandleData>.CandleStickLeftSideLabel() {
    val textMeasurer = rememberTextMeasurer()
    ChartCanvas(
        modifier = Modifier
            .anchor(ChartAnchor.Start)
            .widthIn(min = 42.dp)
            .fillMaxHeight()
    ) {
        val lowest = chartDataset.minOf { it.low }
        val highest = chartDataset.maxOf { it.high }
        var textLayoutResult = textMeasurer.measure(
            text = highest.toString(),
            style = TextStyle(color = Color.Black, fontSize = 16.sp)
        )
        // at the top of the rect.
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                (size.width - textLayoutResult.size.width) / 2f,
                textLayoutResult.size.height / 2f
            )
        )
        // at the bottom of the rect.
        textLayoutResult = textMeasurer.measure(
            text = lowest.toString(),
            style = TextStyle(color = Color.Black, fontSize = 16.sp)
        )
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                (size.width - textLayoutResult.size.width) / 2f,
                size.height - textLayoutResult.size.height / 2f
            )
        )
    }
}