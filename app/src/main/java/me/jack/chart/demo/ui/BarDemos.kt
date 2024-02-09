package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
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
import me.jack.compose.chart.component.BarStickMarkComponent
import me.jack.compose.chart.component.BarStyle
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.component.BarChartContent
import me.jack.compose.chart.component.onDoubleTap
import me.jack.compose.chart.component.onLongPress
import me.jack.compose.chart.component.onTap
import me.jack.compose.chart.component.toPx
import me.jack.compose.chart.measure.fixedContentMeasurePolicy
import me.jack.compose.chart.measure.fixedOverlayContentMeasurePolicy
import me.jack.compose.chart.measure.fixedVerticalContentMeasurePolicy
import me.jack.compose.chart.model.BarData
import me.jack.compose.chart.scope.ChartDataset
import me.jack.compose.chart.model.SimpleBarData
import me.jack.compose.chart.scope.rememberChartDataGroup
import me.jack.compose.chart.scope.forEach
import me.jack.compose.chart.scope.maxOf
import me.jack.compose.chart.scope.rememberMarkedChartDataset
import me.jack.compose.chart.scope.rememberMaxValue
import kotlin.random.Random

class BarDemos {
    @Preview
    @Composable
    fun BarChartDemo() {
        val barDataset = buildChartDataset()
        val context = LocalContext.current
        Column(modifier = Modifier) {
            BarChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedContentMeasurePolicy(32.dp.toPx(), 8.dp.toPx(), 16.dp.toPx()),
                chartDataset = barDataset,
                tapGestures = TapGestures<BarData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            BarChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedVerticalContentMeasurePolicy(32.dp.toPx(), 8.dp.toPx(), 16.dp.toPx()),
                chartDataset = barDataset,
                tapGestures = TapGestures<BarData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    @Preview
    @Composable
    fun StackBarChartDemo() {
        val barDataset = buildChartDataset()
        val context = LocalContext.current
        Column(modifier = Modifier) {
            BarChart(
                modifier = Modifier.height(240.dp),
                barStyle = BarStyle.Stack,
                contentMeasurePolicy = fixedOverlayContentMeasurePolicy(
                    32.dp.toPx(),
                    8.dp.toPx(),
                    Orientation.Horizontal
                ),
                chartDataset = barDataset,
                tapGestures = TapGestures<BarData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            BarChart(
                modifier = Modifier.height(240.dp),
                barStyle = BarStyle.Stack,
                contentMeasurePolicy = fixedOverlayContentMeasurePolicy(
                    32.dp.toPx(),
                    8.dp.toPx(),
                    Orientation.Vertical
                ),
                chartDataset = barDataset,
                tapGestures = TapGestures<BarData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    @Preview
    @Composable
    fun BarMarkChartDemo() {
        val barDataset: ChartDataset<BarData> = rememberChartDataGroup {
            repeat(3) { chartIndex ->
                dataset("Group:$chartIndex") {
                    items(5) {
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
        val context = LocalContext.current
        val markedChartDataset1 = rememberMarkedChartDataset()
        val markedChartDataset2 = rememberMarkedChartDataset()
        Column(modifier = Modifier) {
            val maxValue = barDataset.rememberMaxValue { it.value }
            BarChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedContentMeasurePolicy(
                    32.dp.toPx(),
                    8.dp.toPx()
                ),
                chartDataset = barDataset,
                tapGestures = TapGestures<BarData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            ) {
                BarChartContent()
                BarStickMarkComponent(
                    markedChartDataset = markedChartDataset1,
                    color = MaterialTheme.colorScheme.primary
                )
                BarStickMarkComponent(
                    markedChartDataset = markedChartDataset2,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            val markedIndices = remember { mutableMapOf<Int, MutableList<Int>>() }
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Button(
                    onClick = {
                        val chartGroupIndex = Random.nextInt(barDataset.groupSize)
                        val index = Random.nextInt(barDataset.size)
                        val indices = markedIndices.getOrPut(chartGroupIndex) { mutableListOf() }
                        indices.add(index)
                        markedChartDataset1.addMarkedData(
                            chartGroupIndex = chartGroupIndex,
                            index = index,
                            value = Random.nextInt(maxValue.toInt()).toFloat()
                        )
                        markedChartDataset2.addMarkedData(
                            chartGroupIndex = chartGroupIndex,
                            index = index,
                            value = Random.nextInt(maxValue.toInt()).toFloat()
                        )
                    }
                ) {
                    Text(text = "Random Add")
                }
                Button(
                    onClick = {
                        if (markedIndices.keys.isNotEmpty()) {
                            val chartGroupIndex = markedIndices.keys.last()
                            val index = markedIndices[chartGroupIndex]?.lastOrNull()
                            if (null != index) {
                                markedChartDataset1.removeMarkedData(
                                    chartGroupIndex = chartGroupIndex,
                                    index = index
                                )
                                markedChartDataset2.removeMarkedData(
                                    chartGroupIndex = chartGroupIndex,
                                    index = index
                                )
                            }
                        }
                    }
                ) {
                    Text(text = "Random Remove")
                }
                Button(
                    onClick = {
                        markedChartDataset1.clearMarkedData()
                        markedChartDataset2.clearMarkedData()
                    }
                ) {
                    Text(text = "Clear")
                }
            }
        }
    }

    @Preview
    @Composable
    fun AnimatableBarChartDemo() {
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
                contentMeasurePolicy = fixedContentMeasurePolicy(32.dp.toPx(), 8.dp.toPx(), 16.dp.toPx()),
                chartDataset = barDataset,
                tapGestures = TapGestures<BarData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }.onDoubleTap { currentItem ->
                    Toast.makeText(context, "onDoubleTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }.onLongPress { currentItem ->
                    Toast.makeText(context, "onLongPress:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    @Composable
    private fun buildChartDataset(): ChartDataset<BarData> {
        return rememberChartDataGroup {
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

    @Composable
    private fun buildAnimatableChartDataset(scope: CoroutineScope): ChartDataset<BarData> {
        return rememberChartDataGroup {
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