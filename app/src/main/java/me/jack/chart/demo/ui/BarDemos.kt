package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import me.jack.compose.chart.component.BarChart
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.component.onTap
import me.jack.compose.chart.component.toPx
import me.jack.compose.chart.measure.fixedCrossAxisContentMeasurePolicy
import me.jack.compose.chart.measure.fixedMainAxisContentMeasurePolicy
import me.jack.compose.chart.model.BarData
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.SimpleBarData
import me.jack.compose.chart.model.chartDataGroup
import me.jack.compose.chart.model.forEach
import me.jack.compose.chart.scope.fastForEach
import kotlin.random.Random

class BarDemos {
    @Preview
    @Composable
    fun BarChartSimpleUsage() {
        val barDataset = buildChartDataset()
        val context = LocalContext.current
        Column {
            BarChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedCrossAxisContentMeasurePolicy(32.dp.toPx(), 8.dp.toPx(), 16.dp.toPx()),
                chartDataset = barDataset,
                tapGestures = TapGestures<BarData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            BarChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedMainAxisContentMeasurePolicy(32.dp.toPx(), 8.dp.toPx(), 16.dp.toPx()),
                chartDataset = barDataset,
                tapGestures = TapGestures<BarData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    @Preview
    @Composable
    fun AnimatableBarChart() {
        val scope = rememberCoroutineScope()
        val barDataset = buildAnimatableChartDataset(scope = scope)
        val context = LocalContext.current
        SideEffect {
            scope.launch {
                while (true) {
                    delay(1000L)
                    barDataset.forEach { _, barData ->
                        barData.value = 10 + Random.nextInt(10, 50).toFloat()
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            BarChart(
                modifier = Modifier.height(360.dp),
                contentMeasurePolicy = fixedCrossAxisContentMeasurePolicy(32.dp.toPx(), 8.dp.toPx(), 16.dp.toPx()),
                chartDataset = barDataset,
                tapGestures = TapGestures<BarData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun buildChartDataset(): ChartDataset<BarData> {
        return chartDataGroup {
            repeat(3) { chartIndex ->
                dataset("Group:$chartIndex") {
                    items(50) {
                        SimpleBarData(
                            value = 10 + Random.nextInt(10, 50).toFloat(),
                            color = Color(
                                Random.nextInt(0, 255),
                                Random.nextInt(0, 255),
                                Random.nextInt(0, 255),
                                0xFF
                            )
                        )
                    }
                }
            }
        }
    }

    private fun buildAnimatableChartDataset(scope: CoroutineScope): ChartDataset<BarData> {
        return chartDataGroup {
            repeat(3) { chartIndex ->
                animatableDataset(scope, "Group:$chartIndex") {
                    items(50) {
                        SimpleBarData(
                            value = 10 + Random.nextInt(10, 50).toFloat(),
                            color = Color(
                                Random.nextInt(0, 255),
                                Random.nextInt(0, 255),
                                Random.nextInt(0, 255),
                                0xFF
                            )
                        )
                    }
                }
            }
        }
    }
}