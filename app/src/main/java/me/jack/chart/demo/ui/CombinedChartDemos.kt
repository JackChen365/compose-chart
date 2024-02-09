package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.jack.compose.chart.component.ChartGridDividerComponent
import me.jack.compose.chart.component.ChartIndicatorComponent
import me.jack.compose.chart.component.CombinedChart
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.component.onDoubleTap
import me.jack.compose.chart.component.onLongPress
import me.jack.compose.chart.component.onTap
import me.jack.compose.chart.component.toPx
import me.jack.compose.chart.context.ChartContext
import me.jack.compose.chart.context.chartInteraction
import me.jack.compose.chart.context.scrollable
import me.jack.compose.chart.measure.fixedContentMeasurePolicy
import me.jack.compose.chart.model.BarData
import me.jack.compose.chart.scope.ChartDataset
import me.jack.compose.chart.model.LineData
import me.jack.compose.chart.model.SimpleBarData
import me.jack.compose.chart.model.SimpleLineData
import me.jack.compose.chart.scope.forEach
import me.jack.compose.chart.scope.forEachGroup
import me.jack.compose.chart.scope.rememberSimpleChartDataset
import kotlin.random.Random

class CombinedChartDemos {

    @Composable
    private fun buildLineChartDataset(
        barDataset: ChartDataset<BarData>
    ): ChartDataset<LineData> {
        val dataset = rememberSimpleChartDataset<LineData>()
        barDataset.forEachGroup { chartGroup ->
            val newDataset = mutableListOf<LineData>()
            barDataset.forEach(chartGroup) { data ->
                newDataset.add(SimpleLineData(value = data.value, color = data.color))
            }
            dataset.addGroupData(chartGroup, newDataset)
        }
        return dataset
    }

    @Composable
    private fun buildBarChartDataset(): ChartDataset<BarData> {
        val barDataset = rememberSimpleChartDataset<BarData>()
        repeat(3) {
            val barDataList = mutableListOf<BarData>()
            val groupColor = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
            repeat(50) {
                barDataList.add(
                    SimpleBarData(
                        value = 10 + Random.nextInt(10, 50).toFloat(),
                        color = groupColor
                    )
                )
            }
            barDataset.addGroupData("Group:$it", barDataList)
        }
        return barDataset
    }

    @Preview
    @Composable
    fun CombinedChartPreview() {
        val context = LocalContext.current
        CombinedChart(
            modifier = Modifier.height(320.dp),
            chartContext = ChartContext
                .scrollable(rememberScrollState())
                .chartInteraction(MutableInteractionSource()),
            contentMeasurePolicy = fixedContentMeasurePolicy(fixedRowSize = 32.dp.toPx()),
            componentContent = {
                val barDataset = buildBarChartDataset()
                barChart(
                    chartDataset = barDataset,
                    tapGestures = TapGestures<BarData>().onTap { currentItem ->
                        Toast.makeText(context, "on BarData Tap:${currentItem}", Toast.LENGTH_SHORT).show()
                    }.onLongPress { currentItem ->
                        Toast.makeText(context, "on BarData LongPress:${currentItem}", Toast.LENGTH_SHORT).show()
                    }.onDoubleTap { currentItem ->
                        Toast.makeText(context, "on BarData Double tap:${currentItem}", Toast.LENGTH_SHORT).show()
                    }
                )
                lineChart(
                    chartDataset = buildLineChartDataset(barDataset),
                    tapGestures = TapGestures<LineData>().onTap { currentItem ->
                        Toast.makeText(context, "on LineData Tap:${currentItem}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        ) {
            ChartIndicatorComponent()
            ChartGridDividerComponent()
        }
    }
}