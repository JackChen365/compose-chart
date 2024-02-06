package me.jack.compose.chart.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.jack.compose.chart.context.ChartContext
import me.jack.compose.chart.measure.ChartContentMeasurePolicy
import me.jack.compose.chart.model.BarData
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.LineData
import me.jack.compose.chart.scope.ChartScope
import me.jack.compose.chart.scope.SingleChartScope

class ChartComponent<T>(
    val chartDataset: ChartDataset<T>,
    val tapGestures: TapGestures<T>,
    val content: @Composable (SingleChartScope<T>.() -> Unit) = { }
)

class ChartComponentScope {
    internal val chartComponents = mutableListOf<ChartComponent<*>>()
    fun barChart(
        chartDataset: ChartDataset<BarData>,
        tapGestures: TapGestures<BarData>,
        content: @Composable SingleChartScope<BarData>.() -> Unit = {
            BarComponent()
        }
    ) {
        chartComponents.add(
            ChartComponent(
                chartDataset = chartDataset,
                tapGestures = tapGestures,
                content = content
            )
        )
    }

    fun lineChart(
        chartDataset: ChartDataset<LineData>,
        tapGestures: TapGestures<LineData>,
        content: @Composable SingleChartScope<LineData>.() -> Unit = {
            ChartLineComponent()
        }
    ) {
        chartComponents.add(
            ChartComponent(
                chartDataset = chartDataset,
                tapGestures = tapGestures,
                content = content
            )
        )
    }
}

@Composable
fun CombinedChart(
    modifier: Modifier = Modifier,
    chartContext: ChartContext = ChartContext,
    contentMeasurePolicy: ChartContentMeasurePolicy,
    componentContent: @Composable ChartComponentScope.() -> Unit = { },
    content: @Composable ChartScope.() -> Unit = { }
) {
    val componentScope = ChartComponentScope()
    componentContent.invoke(componentScope)
    @Suppress("UNCHECKED_CAST")
    CombinedChartLayout(
        modifier = modifier,
        chartContext = chartContext,
        contentMeasurePolicy = contentMeasurePolicy,
        chartComponents = componentScope.chartComponents as List<ChartComponent<Any>>,
        content = content
    )
}