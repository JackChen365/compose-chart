package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.jack.compose.chart.component.BubbleChart
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.component.onTap
import me.jack.compose.chart.component.toPx
import me.jack.compose.chart.measure.fixedContentMeasurePolicy
import me.jack.compose.chart.measure.fixedVerticalContentMeasurePolicy
import me.jack.compose.chart.model.BubbleData
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.SimpleBubbleData
import me.jack.compose.chart.model.rememberChartDataGroup
import kotlin.random.Random

class BubbleDemos {

    @Composable
    private fun buildChartDataset(): ChartDataset<BubbleData> {
        return rememberChartDataGroup {
            repeat(3) { groupIndex ->
                val groupColor = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
                dataset("Group:$groupIndex") {
                    items(50) {
                        SimpleBubbleData(
                            label = "Label$groupIndex-$it",
                            value = Random.nextInt(10, 100).toFloat(),
                            volume = Random.nextInt(2, 12).toFloat(),
                            color = groupColor
                        )
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun BubbleChartPreview() {
        val context = LocalContext.current
        Column {
            BubbleChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedContentMeasurePolicy(32.dp.toPx()),
                chartDataset = buildChartDataset(),
                tapGestures = TapGestures<BubbleData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:$currentItem", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            BubbleChart(
                modifier = Modifier.height(240.dp),
                contentMeasurePolicy = fixedVerticalContentMeasurePolicy(32.dp.toPx()),
                chartDataset = buildChartDataset(),
                tapGestures = TapGestures<BubbleData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:$currentItem", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}