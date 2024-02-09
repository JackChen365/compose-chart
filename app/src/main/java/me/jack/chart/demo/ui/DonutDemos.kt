package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.jack.compose.chart.component.DonutChart
import me.jack.compose.chart.component.DonutData
import me.jack.compose.chart.component.SimpleDonutData
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.component.onTap
import me.jack.compose.chart.scope.ChartDataset
import me.jack.compose.chart.scope.asChartDataset
import me.jack.compose.chart.scope.rememberSimpleChartDataset
import kotlin.random.Random

class DonutDemos {
    @Composable
    private fun buildChartDataset(): ChartDataset<DonutData> {
        val dataset = rememberSimpleChartDataset<DonutData>()
        repeat(3) {
            val dataList = mutableListOf<DonutData>()
            repeat(5) { index ->
                dataList.add(
                    SimpleDonutData(
                        label = "Label:$index",
                        value = Random.nextInt(30, 1000).toFloat(),
                        color = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
                    )
                )
            }
            dataset.addGroupData("Group:$it", dataList)
        }
        return dataset
    }

    private fun buildChartSingleDataset(): ChartDataset<DonutData> {
        val dataList = mutableListOf<DonutData>()
        repeat(5) { index ->
            dataList.add(
                SimpleDonutData(
                    label = "Label:$index",
                    value = Random.nextInt(30, 1000).toFloat(),
                    color = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
                )
            )
        }
        return dataList.asChartDataset()
    }

    @Preview
    @Composable
    fun DonutChartDemo() {
        val context = LocalContext.current
        Column {
            DonutChart(
                modifier = Modifier
                    .height(240.dp)
                    .background(color = Color.Gray),
                chartDataset = buildChartDataset(),
                tapGestures = TapGestures<DonutData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DonutChart(
                modifier = Modifier
                    .height(240.dp)
                    .background(color = Color.Gray),
                chartDataset = buildChartSingleDataset(),
                tapGestures = TapGestures<DonutData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}