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
import me.jack.compose.chart.context.ChartScrollableState
import me.jack.compose.chart.context.chartInteraction
import me.jack.compose.chart.context.scrollable
import me.jack.compose.chart.context.zoom
import me.jack.compose.chart.draw.ChartCanvas
import me.jack.compose.chart.draw.DrawElement
import me.jack.compose.chart.draw.interaction.pressInteractionState
import me.jack.compose.chart.interaction.asPressInteraction
import me.jack.compose.chart.measure.ChartContentMeasurePolicy
import me.jack.compose.chart.model.BubbleData
import me.jack.compose.chart.scope.ChartDataset
import me.jack.compose.chart.scope.SingleChartScope
import me.jack.compose.chart.scope.fastForEach
import me.jack.compose.chart.scope.isHorizontal
import me.jack.compose.chart.scope.rememberMaxValue

class BubbleSpec(
    val maxRadius: Dp = 40.dp,
)

val BubbleChartContent: @Composable SingleChartScope<BubbleData>.() -> Unit = {
    ChartBorderComponent()
    ChartGridDividerComponent()
    ChartIndicatorComponent()
    ChartContent()
}

@Composable
fun SimpleBubbleChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<BubbleData>,
    bubbleSpec: BubbleSpec = BubbleSpec(),
    contentMeasurePolicy: ChartContentMeasurePolicy,
    tapGestures: TapGestures<BubbleData> = TapGestures(),
    content: @Composable SingleChartScope<BubbleData>.() -> Unit = simpleChartContent
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
    scrollableState: ChartScrollableState? = null,
    content: @Composable SingleChartScope<BubbleData>.() -> Unit = BubbleChartContent
) {
    SingleChartLayout(
        modifier = modifier,
        chartContext = ChartContext.chartInteraction(remember { MutableInteractionSource() })
            .scrollable(
                scrollableState = rememberScrollState(),
                orientation = contentMeasurePolicy.orientation
            )
            .zoom(),
        tapGestures = tapGestures,
        contentMeasurePolicy = contentMeasurePolicy,
        scrollableState = scrollableState,
        chartDataset = chartDataset,
        content = content
    ) {
        BubbleComponent(bubbleSpec)
        BubbleMarkerComponent()
    }
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
    val maxValue = chartDataset.rememberMaxValue { it.value }
    val maxVolume = chartDataset.rememberMaxValue { it.volume }
    val volumeSize = bubbleSpec.maxRadius.toPx() / maxVolume
    ChartCanvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val bubbleItemSize = size.height / maxValue
        fastForEach { current ->
            clickable {
                drawCircle(
                    color = current.color whenPressedAnimateTo current.color.copy(alpha = 0.8f),
                    radius = (current.volume * volumeSize) whenPressedAnimateTo (current.volume * volumeSize * 1.2f),
                    center = if (isHorizontal) Offset(
                        x = childCenterOffset.x,
                        y = size.crossAxis - current.value * bubbleItemSize
                    ) else Offset(
                        x = size.crossAxis - current.value * bubbleItemSize,
                        y = childCenterOffset.y
                    )
                )
            }
        }
    }
}