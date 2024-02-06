package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.jack.compose.chart.component.PieChart
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.component.onTap
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.PieData
import me.jack.compose.chart.model.SINGLE_GROUP_NAME
import me.jack.compose.chart.model.SimplePieData
import me.jack.compose.chart.model.rememberChartDataGroup
import kotlin.random.Random

class PieDemos {

    @Preview
    @Composable
    fun PieChartPreview() {
        val context = LocalContext.current
        Column {
            PieChart(
                modifier = Modifier.height(240.dp),
                chartDataset = buildChartSingleDataset(),
                tapGestures = TapGestures<PieData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:$currentItem", Toast.LENGTH_SHORT).show()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PieChart(
                modifier = Modifier
                    .height(240.dp)
                    .padding(12.dp),
                chartDataset = buildChartDataset(),
                tapGestures = TapGestures<PieData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:$currentItem", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    @Composable
    private fun buildChartDataset(): ChartDataset<PieData> {
        return rememberChartDataGroup {
            repeat(3) {
                val groupColor = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
                dataset("Group:$it") {
                    items(3) { index ->
                        SimplePieData(
                            label = "Label:$index",
                            value = Random.nextInt(30, 1000).toFloat(),
                            color = groupColor
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun buildChartSingleDataset(): ChartDataset<PieData> {
        return rememberChartDataGroup {
            dataset(SINGLE_GROUP_NAME) {
                items(5) { index ->
                    SimplePieData(
                        label = "Label:$index",
                        value = Random.nextInt(30, 1000).toFloat(),
                        color = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
                    )
                }
            }
        }
    }
}