package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
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
import me.jack.compose.chart.component.CurveLineChart
import me.jack.compose.chart.component.LineChart
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.component.onTap
import me.jack.compose.chart.component.toPx
import me.jack.compose.chart.measure.fixedCrossAxisOverlayContentMeasurePolicy
import me.jack.compose.chart.model.LineData
import me.jack.compose.chart.model.SimpleLineData
import me.jack.compose.chart.model.chartDataGroup
import me.jack.compose.chart.model.forEach
import me.jack.compose.chart.scope.SingleChartScope
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
            LineChart(modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedCrossAxisOverlayContentMeasurePolicy(32.dp.toPx()),
                chartDataset = dataset,
                tapGestures = TapGestures<LineData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                })
            CurveLineChart(modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedCrossAxisOverlayContentMeasurePolicy(32.dp.toPx()),
                chartDataset = dataset,
                tapGestures = TapGestures<LineData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                })
        }
    }

    @Preview
    @Composable
    fun LineDataAnimationPreview() {
        val scope = rememberCoroutineScope()
        val animatableDataset = chartDataGroup<LineData> {
            repeat(3) {
                val groupColor = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
                animatableDataset(scope, "Group:$it") {
                    items(50) {
                        SimpleLineData(
                            value = Random.nextInt(30, 100).toFloat(), color = groupColor
                        )
                    }
                }
            }
        }
        SideEffect {
            scope.launch {
                while (true) {
                    delay(1000L)
                    animatableDataset.forEach { _, barData ->
                        barData.value = 10 + Random.nextInt(10, 50).toFloat()
                    }
                }
            }
        }
        val context = LocalContext.current
        Column {
            LineChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedCrossAxisOverlayContentMeasurePolicy(32.dp.toPx()),
                chartDataset = animatableDataset,
                tapGestures = TapGestures<LineData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
            CurveLineChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedCrossAxisOverlayContentMeasurePolicy(32.dp.toPx()),
                chartDataset = animatableDataset,
                tapGestures = TapGestures<LineData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

}