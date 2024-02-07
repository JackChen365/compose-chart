package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jack.compose.chart.component.CurveLineChart
import me.jack.compose.chart.component.CurveLineChartContent
import me.jack.compose.chart.component.LineChart
import me.jack.compose.chart.component.LineChartContent
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.component.onTap
import me.jack.compose.chart.component.toPx
import me.jack.compose.chart.measure.fixedOverlayContentMeasurePolicy
import me.jack.compose.chart.model.LineData
import me.jack.compose.chart.model.SimpleLineData
import me.jack.compose.chart.scope.ChartDataset
import me.jack.compose.chart.scope.LineChartScope
import me.jack.compose.chart.scope.fastForEach
import me.jack.compose.chart.scope.forEachGroup
import me.jack.compose.chart.scope.rememberChartDataGroup
import me.jack.compose.chart.scope.rememberChartMutableDataGroup
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class LineDemos {

    @Preview
    @Composable
    fun LineChartPreview() {
        val dataset = rememberChartDataGroup<LineData> {
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
    fun LineChartWithMutableDataPreview() {
        var groupCounter by remember {
            mutableIntStateOf(0)
        }
        val dataset = rememberChartMutableDataGroup<LineData> {
            val groupColor = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
            dataset("Group${groupCounter++}") {
                items(5) {
                    SimpleLineData(
                        value = Random.nextInt(30, 100).toFloat(), color = groupColor
                    )
                }
            }
        }
        val context = LocalContext.current
        Column {
            CurveLineChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedOverlayContentMeasurePolicy(32.dp.toPx()),
                chartDataset = dataset,
                tapGestures = TapGestures<LineData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                Button(
                    onClick = {
                        val newChartGroupData = mutableListOf<LineData>()
                        repeat(dataset.size) {
                            newChartGroupData.add(
                                SimpleLineData(
                                    value = Random.nextInt(30, 100).toFloat(),
                                    color = Color(
                                        Random.nextInt(0, 255),
                                        Random.nextInt(0, 255),
                                        Random.nextInt(0, 255),
                                        0xFF
                                    )
                                )
                            )
                        }
                        dataset.addGroupData(
                            chartGroup = "Group${groupCounter++}",
                            chartData = newChartGroupData
                        )
                    }
                ) {
                    Text(text = "Add")
                }
                Button(
                    onClick = {
                        if (0 < groupCounter) {
                            dataset.removeGroupData(
                                chartGroup = "Group${--groupCounter}"
                            )
                        }
                    }
                ) {
                    Text(text = "Remove")
                }
                Button(
                    onClick = {
                        dataset.forEachGroup { chartGroup ->
                            val last = dataset[chartGroup].lastOrNull()
                            dataset.addData(
                                chartGroup = chartGroup,
                                chartData = SimpleLineData(
                                    value = Random.nextInt(30, 100).toFloat(),
                                    color = last?.color ?: Color(
                                        Random.nextInt(0, 255),
                                        Random.nextInt(0, 255),
                                        Random.nextInt(0, 255),
                                        0xFF
                                    )
                                )
                            )
                        }
                    }
                ) {
                    Text(text = "Add item")
                }
                Button(
                    onClick = {
                        dataset.forEachGroup { chartGroup ->
                            val dataList = dataset[chartGroup]
                            dataset.removeData(
                                chartGroup = chartGroup,
                                index = dataList.lastIndex
                            )
                        }
                    }
                ) {
                    Text(text = "Remove item")
                }
            }
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
                LineChartContent()
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
                CurveLineChartContent()
            }
        }
    }

    @Composable
    private fun animatableDataset(scope: CoroutineScope): ChartDataset<LineData> {
        val animatableDataset = rememberChartDataGroup<LineData> {
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