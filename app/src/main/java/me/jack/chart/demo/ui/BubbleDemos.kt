package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
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
import me.jack.compose.chart.measure.fixedCrossAxisContentMeasurePolicy
import me.jack.compose.chart.model.BubbleData
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.SimpleBubbleData
import me.jack.compose.chart.model.simpleChartDataset
import kotlin.random.Random

class BubbleDemos {

    private fun buildChartDataset(): ChartDataset<BubbleData> {
        val dataset = simpleChartDataset<BubbleData>()
        repeat(3) { groupIndex ->
            val dataList = mutableListOf<BubbleData>()
            val groupColor = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
            repeat(50) {
                dataList.add(
                    SimpleBubbleData(
                        label = "Label$groupIndex-$it",
                        value = Random.nextInt(10, 100).toFloat(),
                        volume = Random.nextInt(2, 12).toFloat(),
                        color = groupColor
                    )
                )
            }
            dataset.addChartData("Group:$groupIndex", dataList)
        }
        return dataset
    }

    @Preview
    @Composable
    fun BubbleChartPreview() {
        val context = LocalContext.current
        Column {
            BubbleChart(
                modifier = Modifier.height(360.dp),
                contentMeasurePolicy = fixedCrossAxisContentMeasurePolicy(32.dp.toPx()),
                chartDataset = buildChartDataset(),
                tapGestures = TapGestures<BubbleData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:$currentItem", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}