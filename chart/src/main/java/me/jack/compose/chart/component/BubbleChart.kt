package me.jack.compose.chart.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
import me.jack.compose.chart.model.BubbleData
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.forEach
import me.jack.compose.chart.model.forEachGroupIndexed
import me.jack.compose.chart.model.maxOf
import me.jack.compose.chart.scope.SingleChartScope
import me.jack.compose.chart.scope.chartChildOffsets
import me.jack.compose.chart.scope.chartGroupOffsets
import me.jack.compose.chart.scope.getChartGroupOffsets

class BubbleSpec(
    val maxRadius: Dp = 40.dp,
)

@Composable
fun SimpleBubbleChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<BubbleData>,
    bubbleSpec: BubbleSpec = BubbleSpec(),
    contentMeasurePolicy: ChartContentMeasurePolicy,
    tapGestures: TapGestures<BubbleData> = TapGestures(),
    content: @Composable SingleChartScope<BubbleData>.() -> Unit = { ChartContent() }
) {
    BubbleChart(
        modifier = modifier,
        chartDataset = chartDataset,
        bubbleSpec = bubbleSpec,
        contentMeasurePolicy = contentMeasurePolicy,
        tapGestures = tapGestures,
        content = content
    )
}

@Composable
fun BubbleChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<BubbleData>,
    bubbleSpec: BubbleSpec = BubbleSpec(),
    contentMeasurePolicy: ChartContentMeasurePolicy,
    tapGestures: TapGestures<BubbleData> = TapGestures(),
    content: @Composable SingleChartScope<BubbleData>.() -> Unit = {
        ChartBorderComponent()
        ChartGridDividerComponent()
        ChartIndicatorComponent()
        ChartContent()
    }
) {
    SingleChartLayout(
        modifier = modifier,
        chartDataset = chartDataset,
        tapGestures = tapGestures,
        contentMeasurePolicy = contentMeasurePolicy,
        chartContext = ChartContext.chartInteraction(remember { MutableInteractionSource() })
            .scrollable(state = rememberScrollState())
            .zoom(),
        content = content,
        chartContent = {
            BubbleComponent(bubbleSpec)
            BubbleMarkerComponent()
        }
    )
}

@Composable
fun SingleChartScope<BubbleData>.BubbleMarkerComponent() {
    val pressInteraction = chartContext.pressInteractionState.value.asPressInteraction<BubbleData>() ?: return
    val currentItem = pressInteraction.currentItem
    val drawElement = pressInteraction.drawElement
    if (drawElement is DrawElement.Circle) {
        MarkerDashLineComponent(
            leftTop = Offset(
                x = drawElement.center.x - drawElement.radius,
                y = drawElement.center.y - drawElement.radius
            ),
            contentSize = Size(
                width = 2 * drawElement.radius,
                height = 2 * drawElement.radius
            ),
            focusPoint = drawElement.center
        )
        MarkerComponent(
            leftTop = Offset(
                x = drawElement.center.x - drawElement.radius,
                y = drawElement.center.y - drawElement.radius
            ),
            size = Size(
                width = 2 * drawElement.radius,
                height = 2 * drawElement.radius
            ),
            focusPoint = drawElement.center,
            displayInfo = "(" + currentItem.value.toString() + ")"
        )
    }
}

@Composable
private fun SingleChartScope<BubbleData>.BubbleComponent(
    bubbleSpec: BubbleSpec = BubbleSpec()
) {
    val maxValue = remember(chartDataset) {
        chartDataset.maxOf { it.value }
    }
    val maxVolume = remember(chartDataset) {
        chartDataset.maxOf { it.volume }
    }
    val volumeSize = bubbleSpec.maxRadius.toPx() / maxVolume
    ChartCanvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val scrollState = chartContext.requireChartScrollState
        val bubbleItemSize = size.height / maxValue
        chartDataset.forEachGroupIndexed { groupIndex, groupName ->
            var offset: Float = -scrollState.firstVisibleItemOffset + getChartGroupOffsets(groupIndex).mainAxis
            chartDataset.forEach(
                chartGroup = groupName,
                start = scrollState.firstVisibleItem,
                end = scrollState.lastVisibleItem
            ) { current ->
                clickable {
                    drawCircle(
                        color = current.color whenPressedAnimateTo current.color.copy(alpha = 0.8f),
                        radius = (current.volume * volumeSize) whenPressedAnimateTo (current.volume * volumeSize * 1.2f),
                        center = Offset(
                            x = offset + chartChildOffsets.mainAxis / 2,
                            y = size.crossAxis - current.value * bubbleItemSize
                        )
                    )
                }
                offset += chartGroupOffsets.mainAxis
            }
        }
    }
}