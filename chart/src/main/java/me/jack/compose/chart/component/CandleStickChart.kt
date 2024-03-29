package me.jack.compose.chart.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
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
import me.jack.compose.chart.context.ChartScrollableState
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
import me.jack.compose.chart.scope.CandleStickChartScope
import me.jack.compose.chart.scope.ChartAnchor
import me.jack.compose.chart.scope.ChartDataset
import me.jack.compose.chart.scope.SingleChartScope
import me.jack.compose.chart.scope.fastForEach
import me.jack.compose.chart.scope.rememberMaxValue
import me.jack.compose.chart.scope.rememberMinValue
import kotlin.math.absoluteValue
import kotlin.math.min

class CandleStickSpec(
    val candleStickSize: Dp = 8.dp,
    val stackLineSize: Dp = 2.dp,
)

val CancelStickChartContent: @Composable SingleChartScope<CandleData>.() -> Unit =
    {
        CandleStickLeftSideLabel()
        ChartBorderComponent()
        ChartIndicatorComponent()
        ChartGridDividerComponent()
        ChartContent()
    }

@Composable
fun SimpleCandleStickChart(
    modifier: Modifier = Modifier,
    candleStickSize: Dp = 32.dp,
    chartDataset: ChartDataset<CandleData>,
    spec: CandleStickSpec = CandleStickSpec(),
    tapGestures: TapGestures<CandleData> = TapGestures(),
    content: @Composable SingleChartScope<CandleData>.() -> Unit = simpleChartContent
) {
    CandleStickChart(
        modifier = modifier,
        candleStickSize = candleStickSize,
        chartDataset = chartDataset,
        spec = spec,
        tapGestures = tapGestures,
        content = content
    )
}

@Composable
fun CandleStickChart(
    modifier: Modifier = Modifier,
    candleStickSize: Dp = 32.dp,
    chartDataset: ChartDataset<CandleData>,
    spec: CandleStickSpec = CandleStickSpec(),
    tapGestures: TapGestures<CandleData> = TapGestures(),
    scrollableState: ChartScrollableState? = null,
    content: @Composable (SingleChartScope<CandleData>.() -> Unit) = CancelStickChartContent
) {
    val contentMeasurePolicy = fixedContentMeasurePolicy(candleStickSize.toPx())
    SingleChartLayout(
        modifier = modifier,
        chartContext = ChartContext
            .chartInteraction(
                remember { MutableInteractionSource() }
            ).scrollable(
                orientation = contentMeasurePolicy.orientation,
                scrollableState = rememberScrollState()
            )
            .zoom(),
        tapGestures = tapGestures,
        contentMeasurePolicy = contentMeasurePolicy,
        scrollableState = scrollableState,
        chartDataset = chartDataset,
        content = content
    ) {
        CandleStickComponent(spec)
        CandleDataMarkerComponent()
    }
}

@Composable
fun CandleStickChartScope.CandleStickComponent(
    spec: CandleStickSpec = CandleStickSpec()
) {
    val scrollState = chartContext.requireChartScrollState
    val candleStickWidth = spec.candleStickSize.toPx()
    val stickLineWidth = spec.stackLineSize.toPx()
    val highestValue = chartDataset.rememberMaxValue { it.high }
    ChartCanvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val candleBlockSize = size.height / highestValue
        var offset = -scrollState.firstVisibleItemOffset
        // we calculate the lastVisibleItemIndex due to other places need it.
        fastForEach { candleData ->
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
fun CandleStickChartScope.CandleDataMarkerComponent() {
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
fun CandleStickChartScope.CandleStickLeftSideLabel() {
    val textMeasurer = rememberTextMeasurer()
    val lowest = chartDataset.rememberMinValue { it.low }
    val highest = chartDataset.rememberMaxValue { it.high }
    ChartCanvas(
        modifier = Modifier
            .anchor(ChartAnchor.Start)
            .widthIn(min = 42.dp)
            .fillMaxHeight()
    ) {
        var textLayoutResult = textMeasurer.measure(
            text = if (0 == chartDataset.size) "#" else highest.toString(),
            style = TextStyle(color = Color.Black, fontSize = 16.sp)
        )
        // at the top of the rect.
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset((size.width - textLayoutResult.size.width) / 2f, 0f)
        )
        // at the bottom of the rect.
        textLayoutResult = textMeasurer.measure(
            text = if (0 == chartDataset.size) "#" else lowest.toString(),
            style = TextStyle(color = Color.Black, fontSize = 16.sp)
        )
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                (size.width - textLayoutResult.size.width) / 2f,
                size.height - textLayoutResult.size.height
            )
        )
    }
}

@Composable
fun CandleStickChartScope.CandleStickBarComponent(
    context: ChartContext = chartContext,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val maxValue = chartDataset.rememberMaxValue { it.high }
    ChartBox(
        modifier = Modifier
            .anchor(ChartAnchor.Bottom)
            .height(120.dp)
            .clipToBounds(),
        chartContext = context
    ) {
        ChartCanvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val cancelStickSize = size.crossAxis / maxValue
            fastForEach { cancelStickData ->
                drawRect(
                    color = color whenPressedAnimateTo color.copy(alpha = 0.4f),
                    topLeft = Offset(currentLeftTopOffset.x, size.height - cancelStickSize * cancelStickData.open),
                    size = Size(childSize.mainAxis, cancelStickSize * cancelStickData.open)
                )
            }
        }
    }
}