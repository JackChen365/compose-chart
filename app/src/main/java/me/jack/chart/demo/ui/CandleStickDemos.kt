package me.jack.chart.demo.ui

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.jack.compose.chart.component.CancelStickChartContent
import me.jack.compose.chart.component.CandleStickBarComponent
import me.jack.compose.chart.component.CandleStickChart
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.component.onTap
import me.jack.compose.chart.context.ChartInteractionHandler
import me.jack.compose.chart.context.ChartScrollableState
import me.jack.compose.chart.context.ChartZoomState
import me.jack.compose.chart.context.rememberScrollableState
import me.jack.compose.chart.model.CandleData
import me.jack.compose.chart.model.SimpleCandleData
import me.jack.compose.chart.scope.ChartDataset
import me.jack.compose.chart.scope.MutableChartDataset
import me.jack.compose.chart.scope.SINGLE_GROUP_NAME
import me.jack.compose.chart.scope.SingleChartScope
import me.jack.compose.chart.scope.currentRange
import me.jack.compose.chart.scope.rememberChartMutableDataGroup
import kotlin.random.Random

class CandleStickDemos {
    @Preview
    @Composable
    fun CandleStickChartDemo() {
        val chartDataset: ChartDataset<CandleData> = rememberChartMutableDataGroup {
            dataset("Group") {
                items(50) {
                    val low = Random.nextInt(50)
                    val high = Random.nextInt(low + 10, 100)
                    val start = Random.nextInt(low, low + (high - low) / 2)
                    val end = Random.nextInt(low + (high - low) / 2, high)
                    val win = Random.nextBoolean()
                    SimpleCandleData(
                        high = high.toFloat(),
                        low = low.toFloat(),
                        open = if (win) end.toFloat() else start.toFloat(),
                        close = if (!win) end.toFloat() else start.toFloat()
                    )
                }
            }
        }
        val context = LocalContext.current
        Column {
            CandleStickChart(modifier = Modifier.requiredHeight(240.dp),
                candleStickSize = 24.dp,
                chartDataset = chartDataset,
                tapGestures = TapGestures<CandleData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }) {
                CancelStickChartContent()
                CandleStickBarComponent(
                    context = chartContext.minusKey(ChartZoomState).minusKey(ChartInteractionHandler)
                )
            }
        }
    }

    @Preview
    @Composable
    fun CandleStickChartMutableDataDemo() {
        val chartDataset: MutableChartDataset<CandleData> = rememberChartMutableDataGroup {
            dataset(SINGLE_GROUP_NAME) {
                items(50) {
                    val low = Random.nextInt(50)
                    val high = Random.nextInt(low + 10, 100)
                    val start = Random.nextInt(low, low + (high - low) / 2)
                    val end = Random.nextInt(low + (high - low) / 2, high)
                    val win = Random.nextBoolean()
                    SimpleCandleData(
                        high = high.toFloat(),
                        low = low.toFloat(),
                        open = if (win) end.toFloat() else start.toFloat(),
                        close = if (!win) end.toFloat() else start.toFloat()
                    )
                }
            }
        }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val scrollableState = rememberScrollableState()
        Column {
            CandleStickChart(modifier = Modifier.requiredHeight(240.dp),
                candleStickSize = 24.dp,
                chartDataset = chartDataset,
                scrollableState = scrollableState,
                tapGestures = TapGestures<CandleData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                })
            var positionText by remember {
                mutableStateOf("")
            }
            Text(
                text = positionText,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            )
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .align(Alignment.End)
            ) {
                Button(onClick = {
                    val low = Random.nextInt(50)
                    val high = Random.nextInt(low + 10, 100)
                    val start = Random.nextInt(low, low + (high - low) / 2)
                    val end = Random.nextInt(low + (high - low) / 2, high)
                    val win = Random.nextBoolean()
                    chartDataset.addData(
                        chartGroup = SINGLE_GROUP_NAME, chartData = SimpleCandleData(
                            high = high.toFloat(),
                            low = low.toFloat(),
                            open = if (win) end.toFloat() else start.toFloat(),
                            close = if (!win) end.toFloat() else start.toFloat()
                        )
                    )
                }) {
                    Text(text = "Add item")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    if (0 < chartDataset.size) {
                        chartDataset.removeData(SINGLE_GROUP_NAME, chartDataset.size - 1)
                    }
                }) {
                    Text(text = "Remove item")
                }
            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .align(Alignment.End)
            ) {
                var currentItem by remember {
                    mutableIntStateOf(20)
                }
                Button(onClick = {
                    coroutineScope.launch {
                        var newTarget = Random.nextInt(0, chartDataset.size - 15)
                        while (currentItem == newTarget) {
                            newTarget = Random.nextInt(0, chartDataset.size - 15)
                        }
                        positionText += "Position:${newTarget + 1}\n"
                        currentItem = newTarget
                        scrollableState.animateScrollToItem(0, currentItem)
                    }
                }) {
                    Text(text = "Animate to next item")
                }
                Button(onClick = {
                    coroutineScope.launch {
                        var newTarget = Random.nextInt(0, chartDataset.size - 15)
                        while (currentItem == newTarget) {
                            newTarget = Random.nextInt(0, chartDataset.size - 15)
                        }
                        positionText += "Position:${newTarget + 1}\n"
                        currentItem = newTarget
                        scrollableState.scrollToItem(0, currentItem)
                    }
                }) {
                    Text(text = "Next item")
                }
            }
        }
    }

    @Preview
    @Composable
    fun CandleStickChartAutoScrollDemo() {
        val chartDataset: MutableChartDataset<CandleData> = rememberChartMutableDataGroup {
            dataset(SINGLE_GROUP_NAME) {
                items(50) {
                    val low = Random.nextInt(50)
                    val high = Random.nextInt(low + 10, 100)
                    val start = Random.nextInt(low, low + (high - low) / 2)
                    val end = Random.nextInt(low + (high - low) / 2, high)
                    val win = Random.nextBoolean()
                    SimpleCandleData(
                        high = high.toFloat(),
                        low = low.toFloat(),
                        open = if (win) end.toFloat() else start.toFloat(),
                        close = if (!win) end.toFloat() else start.toFloat()
                    )
                }
            }
        }
        val context = LocalContext.current
        val scrollableState = rememberScrollableState()
        var isAutoScroll by remember {
            mutableStateOf(true)
        }
        Column {
            CandleStickChart(modifier = Modifier.requiredHeight(240.dp),
                candleStickSize = 24.dp,
                chartDataset = chartDataset,
                scrollableState = scrollableState,
                tapGestures = TapGestures<CandleData>().onTap { currentItem ->
                    Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
                }
            ) {
                if (isAutoScroll) {
                    LaunchAutoScrollJob(chartDataset, scrollableState)
                }
                CancelStickChartContent()
            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .align(Alignment.End)
            ) {
                Button(onClick = { isAutoScroll = true }) {
                    Text(text = "Auto scroll")
                }
                Button(onClick = { isAutoScroll = false }) {
                    Text(text = "Stop")
                }
            }
        }
    }

    @Composable
    private fun SingleChartScope<CandleData>.LaunchAutoScrollJob(
        chartDataset: MutableChartDataset<CandleData>,
        scrollableState: ChartScrollableState
    ) {
        LaunchedEffect(Unit) {
            launch {
                while (true) {
                    delay(1000L)
                    val visibleSize = (currentRange.last - currentRange.first)
                    val nextIndex = Random.nextInt(chartDataset.size - visibleSize)
                    scrollableState.animateScrollToItem(0, nextIndex)
                }
            }
        }
    }
}