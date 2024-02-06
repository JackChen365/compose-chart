package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.jack.compose.chart.component.CandleStickBarComponent
import me.jack.compose.chart.component.CandleStickChart
import me.jack.compose.chart.component.CandleStickLeftSideLabel
import me.jack.compose.chart.component.ChartBorderComponent
import me.jack.compose.chart.component.ChartGridDividerComponent
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.component.onTap
import me.jack.compose.chart.context.ChartInteractionHandler
import me.jack.compose.chart.context.ChartZoomState
import me.jack.compose.chart.model.CandleData
import me.jack.compose.chart.model.SimpleCandleData
import kotlin.random.Random

class CandleStickDemos {
    @Preview
    @Composable
    fun CandleStickChartPreview() {
        val candleDataList = mutableListOf<CandleData>()
        candleDataList.add(
            SimpleCandleData(
                high = 89f,
                low = 23f,
                open = 29f,
                close = 88f
            )
        )
        repeat(50) {
            val low = Random.nextInt(50)
            val high = Random.nextInt(low + 10, 100)
            val start = Random.nextInt(low, low + (high - low) / 2)
            val end = Random.nextInt(low + (high - low) / 2, high)
            val win = Random.nextBoolean()
            candleDataList.add(
                SimpleCandleData(
                    high = high.toFloat(),
                    low = low.toFloat(),
                    open = if (win) end.toFloat() else start.toFloat(),
                    close = if (!win) end.toFloat() else start.toFloat()
                )
            )
        }
        val context = LocalContext.current
        Column {
            CandleStickChart(
                modifier = Modifier.requiredHeight(240.dp),
                chartData = candleDataList,
                candleStickSize = 24.dp,
                tapGestures = TapGestures<CandleData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            ) {
                CandleStickLeftSideLabel()
                ChartGridDividerComponent()
                ChartBorderComponent()
                CandleStickBarComponent(
                    context = chartContext.minusKey(ChartZoomState).minusKey(ChartInteractionHandler)
                )
                ChartContent()
            }
        }
    }
}