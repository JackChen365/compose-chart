package me.jack.compose.chart.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.jack.compose.chart.context.ChartContext
import me.jack.compose.chart.context.chartInteraction
import me.jack.compose.chart.draw.ChartCanvas
import me.jack.compose.chart.measure.boxBoxChartContentMeasurePolicy
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.PieData
import me.jack.compose.chart.model.SimplePieData
import me.jack.compose.chart.model.sumOf
import me.jack.compose.chart.scope.SingleChartScope
import me.jack.compose.chart.scope.fastForEach

typealias DonutData = PieData
typealias SimpleDonutData = SimplePieData

class DonutSpec(
    val padding: PaddingValues = PaddingValues(8.dp),
    val strokeWidth: Dp = 36.dp,
    val pressedScale: Float = 1.1f,
    val pressedAlpha: Float = 0.8f
)

@Composable
fun DonutChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<DonutData>,
    spec: DonutSpec = DonutSpec(),
    tapGestures: TapGestures<DonutData> = TapGestures(),
    content: @Composable SingleChartScope<DonutData>.() -> Unit = { ChartContent() }
) {
    SingleChartLayout(modifier = modifier,
        chartDataset = chartDataset,
        tapGestures = tapGestures,
        contentMeasurePolicy = boxBoxChartContentMeasurePolicy(),
        chartContext = ChartContext.chartInteraction(remember { MutableInteractionSource() }),
        content = { content() }
    ) {
        DonutComponent(spec = spec)
    }
}

@Composable
fun SingleChartScope<DonutData>.DonutComponent(
    modifier: Modifier = Modifier,
    spec: DonutSpec = DonutSpec()
) {
    val maxValue = chartDataset.sumOf { it.value }
    val degreesValue = maxValue / 360f
    ChartCanvas(
        modifier = modifier
            .fillMaxSize()
            .padding(spec.padding)
    ) {
        var angleOffset = 0f
        val strokeWidthPx = spec.strokeWidth.toPx()
        val arcSize = Size(
            width = size.minDimension - strokeWidthPx,
            height = size.minDimension - strokeWidthPx
        )
        fastForEach { pieData ->
            val sweepAngle = pieData.value / degreesValue
            clickable {
                drawArc(
                    color = pieData.color whenPressedAnimateTo pieData.color.copy(alpha = spec.pressedAlpha),
                    startAngle = angleOffset,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        x = (size.width - arcSize.width) / 2,
                        y = (size.height - arcSize.height) / 2
                    ) whenPressedAnimateTo Offset(
                        x = (size.width - arcSize.width * spec.pressedScale) / 2,
                        y = (size.height - arcSize.height * spec.pressedScale) / 2
                    ),
                    size = arcSize whenPressedAnimateTo arcSize.times(spec.pressedScale),
                    style = Stroke(strokeWidthPx)
                )
            }
            angleOffset += sweepAngle
        }
    }
}