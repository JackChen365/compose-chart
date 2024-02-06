package me.jack.compose.chart.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import me.jack.compose.chart.context.ChartContext
import me.jack.compose.chart.context.chartInteraction
import me.jack.compose.chart.context.scrollable
import me.jack.compose.chart.context.zoom
import me.jack.compose.chart.draw.ChartCanvas
import me.jack.compose.chart.draw.DrawElement
import me.jack.compose.chart.draw.interaction.pressInteractionState
import me.jack.compose.chart.interaction.asPressInteraction
import me.jack.compose.chart.measure.ChartContentMeasurePolicy
import me.jack.compose.chart.model.BarData
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.computeGroupTotalValues
import me.jack.compose.chart.model.maxOf
import me.jack.compose.chart.scope.SingleChartScope
import me.jack.compose.chart.scope.fastForEach
import me.jack.compose.chart.scope.fastForEachByIndex
import me.jack.compose.chart.scope.isHorizontal
import me.jack.compose.chart.scope.isLastGroupIndex

enum class BarStyle {
    Normal, Stack
}

@Composable
fun SimpleBarChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<BarData>,
    contentMeasurePolicy: ChartContentMeasurePolicy,
    barStyle: BarStyle = BarStyle.Normal,
    tapGestures: TapGestures<BarData> = TapGestures(),
    content: @Composable SingleChartScope<BarData>.() -> Unit = { ChartContent() }
) {
    BarChart(
        modifier = modifier,
        chartDataset = chartDataset,
        contentMeasurePolicy = contentMeasurePolicy,
        barStyle = barStyle,
        tapGestures = tapGestures,
        content = content
    )
}

@Composable
fun BarChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<BarData>,
    contentMeasurePolicy: ChartContentMeasurePolicy,
    barStyle: BarStyle = BarStyle.Normal,
    tapGestures: TapGestures<BarData> = TapGestures(),
    content: @Composable SingleChartScope<BarData>.() -> Unit = {
        ChartBorderComponent()
        ChartGridDividerComponent()
        ChartAverageAcrossRanksComponent { chartDataset.maxOf { it.value } }
        ChartIndicatorComponent()
        ChartContent()
    }
) {
    SingleChartLayout(
        modifier = modifier,
        chartDataset = chartDataset,
        tapGestures = tapGestures,
        chartContext = ChartContext.chartInteraction(remember { MutableInteractionSource() })
            .scrollable(orientation = contentMeasurePolicy.orientation, state = rememberScrollState())
            .zoom(),
        contentMeasurePolicy = contentMeasurePolicy,
        content = content
    ) {
        when (barStyle) {
            BarStyle.Normal -> {
                BarComponent()
                BubbleMarkerComponent()
            }

            BarStyle.Stack -> {
                BarStackComponent()
                BubbleMarkerComponent()
            }
        }
    }
}

@Composable
fun SingleChartScope<BarData>.BarComponent() {
    val maxValue = remember(chartDataset) {
        chartDataset.maxOf { it.value }
    }
    ChartCanvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val barItemSize = size.crossAxis / maxValue
        fastForEach { barData ->
            clickable {
                if (isHorizontal) {
                    drawRect(
                        color = barData.color whenPressedAnimateTo barData.color.copy(alpha = 0.4f),
                        topLeft = Offset(currentLeftTopOffset.x, size.height - barItemSize * barData.value),
                        size = Size(childSize.mainAxis, size.crossAxis)
                    )
                } else {
                    drawRect(
                        color = barData.color whenPressedAnimateTo barData.color.copy(alpha = 0.4f),
                        topLeft = Offset(0f, currentLeftTopOffset.y),
                        size = Size(barItemSize * barData.value, childSize.mainAxis)
                    )
                }
            }
        }
    }
}

@Composable
fun SingleChartScope<BarData>.BarStackComponent() {
    val sumValueSet = remember(chartDataset) {
        chartDataset.computeGroupTotalValues { it.value }
    }
    val maxValue = remember(sumValueSet) {
        sumValueSet.maxOf { it }
    }
    ChartCanvas(modifier = Modifier.fillMaxSize()) {
        var offset = 0f
        val barItemSize = size.crossAxis / maxValue
        fastForEachByIndex { barData ->
            val (topLeft, rectSize) = if (isHorizontal) {
                Offset(
                    currentLeftTopOffset.x, size.height - offset - barItemSize * barData.value
                ) to Size(childSize.mainAxis, barItemSize * barData.value)
            } else {
                Offset(offset, currentLeftTopOffset.y) to
                        Size(barItemSize * barData.value, childSize.mainAxis)
            }
            clickableRect(currentLeftTopOffset, childSize)
            drawRect(
                color = barData.color whenPressedAnimateTo barData.color.copy(alpha = 0.4f),
                topLeft = topLeft,
                size = rectSize
            )
            offset = if (isLastGroupIndex()) 0f else offset + barItemSize * barData.value
        }
    }
}

@Composable
fun SingleChartScope<BarData>.BubbleMarkerComponent() {
    val pressInteraction = chartContext.pressInteractionState.value.asPressInteraction<BarData>() ?: return
    val currentGroupItems = pressInteraction.currentGroupItems
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
            displayInfo = "(" + currentGroupItems.joinToString { it.value.toString() } + ")"
        )
    }
}