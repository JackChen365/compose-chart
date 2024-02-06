package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jack.compose.chart.component.ChartAverageAcrossRanksComponent
import me.jack.compose.chart.component.ChartBorderComponent
import me.jack.compose.chart.component.ChartGridDividerComponent
import me.jack.compose.chart.component.ChartIndicatorComponent
import me.jack.compose.chart.component.CurveLineChart
import me.jack.compose.chart.component.LineChart
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.component.onTap
import me.jack.compose.chart.component.toPx
import me.jack.compose.chart.measure.fixedOverlayContentMeasurePolicy
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.LineData
import me.jack.compose.chart.model.SimpleLineData
import me.jack.compose.chart.model.chartDataGroup
import me.jack.compose.chart.model.maxOf
import me.jack.compose.chart.scope.LineChartScope
import me.jack.compose.chart.scope.fastForEach
import kotlin.random.Random

class LineDemos {

    @Preview
    @Composable
    fun LineDataPreview() {
        val dataset = chartDataGroup<LineData> {
            repeat(3) {
                val groupColor = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
                dataset("Group:$it") {
                    items(50) {
                        SimpleLineData(
                            value = Random.nextInt(30, 100).toFloat(), color = groupColor
                        )
                    }
                }
            }
        }
        val context = LocalContext.current
        Column {
            LineChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedOverlayContentMeasurePolicy(32.dp.toPx()),
                chartDataset = dataset,
                tapGestures = TapGestures<LineData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            CurveLineChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedOverlayContentMeasurePolicy(32.dp.toPx()),
                chartDataset = dataset,
                tapGestures = TapGestures<LineData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    @Preview
    @Composable
    fun LineDataAnimationPreview() {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        Column {
            LineChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedOverlayContentMeasurePolicy(32.dp.toPx()),
                chartDataset = animatableDataset(scope),
                tapGestures = TapGestures<LineData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            ) {
                LaunchAnimation(scope)
                ChartBorderComponent()
                ChartGridDividerComponent()
                ChartAverageAcrossRanksComponent { chartDataset.maxOf { it.value } }
                ChartIndicatorComponent()
                ChartContent()
            }
            CurveLineChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedOverlayContentMeasurePolicy(32.dp.toPx()),
                chartDataset = animatableDataset(scope),
                tapGestures = TapGestures<LineData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            ) {
                LaunchAnimation(scope)
                ChartBorderComponent()
                ChartGridDividerComponent()
                ChartAverageAcrossRanksComponent { chartDataset.maxOf { it.value } }
                ChartIndicatorComponent()
                ChartContent()
            }
        }
    }

    private fun animatableDataset(scope: CoroutineScope): ChartDataset<LineData> {
        val animatableDataset = chartDataGroup<LineData> {
            repeat(3) {
                val groupColor = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
                animatableDataset(scope, "Group:$it") {
                    items(5000) {
                        SimpleLineData(
                            value = Random.nextInt(30, 100).toFloat(), color = groupColor
                        )
                    }
                }
            }
        }
        return animatableDataset
    }

    @Composable
    private fun LineChartScope.LaunchAnimation(
        scope: CoroutineScope
    ) {
        SideEffect {
            scope.launch {
                while (true) {
                    delay(1000L)
                    fastForEach { current ->
                        current.value = 10 + Random.nextInt(10, 50).toFloat()
                    }
                }
            }
        }
    }
}