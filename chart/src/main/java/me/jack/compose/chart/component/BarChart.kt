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
import me.jack.compose.chart.context.requireChartScrollState
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
import me.jack.compose.chart.model.forEach
import me.jack.compose.chart.model.forEachGroup
import me.jack.compose.chart.model.forEachGroupIndexed
import me.jack.compose.chart.model.maxOf
import me.jack.compose.chart.scope.SingleChartScope
import me.jack.compose.chart.scope.chartChildOffsets
import me.jack.compose.chart.scope.chartChildSize
import me.jack.compose.chart.scope.chartGroupOffsets
import me.jack.compose.chart.scope.getChartGroupOffsets
import me.jack.compose.chart.scope.isHorizontal

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

            BarStyle.Stack -> BarStackComponent()
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
        val scrollState = chartContext.requireChartScrollState
        chartDataset.forEachGroupIndexed { groupIndex, groupName ->
            var offset: Float = -scrollState.firstVisibleItemOffset + getChartGroupOffsets(groupIndex).mainAxis
            chartDataset.forEach(
                chartGroup = groupName,
                start = scrollState.firstVisibleItem,
                end = scrollState.lastVisibleItem
            ) { barData ->
                if (isHorizontal) {
                    clickableRect(
                        topLeft = Offset(offset, 0f),
                        size = Size(chartChildSize.mainAxis, size.height),
                        focusPoint = Offset(
                            x = offset + chartChildSize.mainAxis / 2,
                            y = size.height - barItemSize * barData.value
                        )
                    )
                    drawRect(
                        color = barData.color whenPressedAnimateTo barData.color.copy(alpha = 0.4f),
                        topLeft = Offset(offset, size.height - barItemSize * barData.value),
                        size = Size(chartChildSize.mainAxis, size.height)
                    )
                } else {
                    clickableRect(
                        topLeft = Offset(0f, offset),
                        size = Size(size.width, chartChildSize.mainAxis),
                        focusPoint = Offset(
                            x = barItemSize * barData.value,
                            y = offset
                        )
                    )
                    drawRect(
                        color = barData.color whenPressedAnimateTo barData.color.copy(alpha = 0.4f),
                        topLeft = Offset(0f, offset),
                        Size(barItemSize * barData.value, chartChildSize.mainAxis)
                    )
                }
                offset += chartGroupOffsets.mainAxis
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
    ChartCanvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val barItemSize = size.crossAxis / maxValue
        val scrollState = chartContext.requireChartScrollState
        var crossAxisOffset: Float = -scrollState.firstVisibleItemOffset
        for (visibleItem in scrollState.currentVisibleRange) {
            var mainAxisOffset = 0f
            chartDataset.forEachGroup { groupName ->
                val barData = chartDataset[groupName][visibleItem]
                val (topLeft, size) = if (isHorizontal) {
                    val topLeft = Offset(crossAxisOffset, size.height - barItemSize * barData.value - mainAxisOffset)
                    val size = Size(chartChildSize.mainAxis, barItemSize * barData.value)
                    topLeft to size
                } else {
                    val topLeft = Offset(mainAxisOffset, crossAxisOffset)
                    val size = Size(barItemSize * barData.value, chartChildSize.mainAxis)
                    topLeft to size
                }
                clickableRect(
                    topLeft = topLeft,
                    size = size,
                    focusPoint = Offset(
                        x = topLeft.x + chartChildSize.mainAxis / 2,
                        y = topLeft.y
                    ),
                    currentItem = barData,
                    index = visibleItem
                )
                drawRect(color = barData.color, topLeft = topLeft, size = size)
                mainAxisOffset += barItemSize * barData.value
            }
            crossAxisOffset += chartChildOffsets.mainAxis
        }
    }
}

@Composable
fun SingleChartScope<BarData>.BubbleMarkerComponent() {
    val pressInteraction = chartContext.pressInteractionState.value.asPressInteraction<BarData>() ?: return
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
            displayInfo = "(" + currentItem.value.toString() + ")"
        )
    }
}