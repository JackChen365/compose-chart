package me.jack.compose.chart.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.jack.compose.chart.context.ChartScrollState
import me.jack.compose.chart.context.isElementAvailable
import me.jack.compose.chart.context.requireChartScrollState
import me.jack.compose.chart.scope.ChartAnchor
import me.jack.compose.chart.scope.ChartScope
import me.jack.compose.chart.scope.chartChildDivider
import me.jack.compose.chart.scope.chartGroupDivider
import me.jack.compose.chart.scope.chartGroupOffsets
import me.jack.compose.chart.scope.isHorizontal

@Stable
val pressedColor = Color(0XCCDCDCDC)

/**
 * Specification for ChartBorderComponent
 */
class BorderSpec(
    val strokeWidth: Dp = 2.dp,
    val color: Color = Color.LightGray
)

@Composable
fun ChartScope.ChartBorderComponent(
    spec: BorderSpec = BorderSpec()
) {
    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .anchor(anchor = ChartAnchor.Center)
    ) {
        drawLine(
            color = spec.color,
            start = Offset(0f, 0f),
            end = Offset(0f, size.height),
            strokeWidth = spec.strokeWidth.toPx()
        )
        drawLine(
            color = spec.color,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = spec.strokeWidth.toPx()
        )
    }
}

/**
 * Specification for ChartFixedGridDividerComponent or ChartFloatGridDividerComponent
 */
class GridDividerSpec(
    val rows: Int = 4,
    val columns: Int = 5,
    val strokeWidth: Dp = 1.dp,
    val color: Color = Color.LightGray
)

@Composable
fun ChartScope.ChartGridDividerComponent(
    spec: GridDividerSpec = GridDividerSpec()
) {
    if (chartContext.isElementAvailable(ChartScrollState)) {
        ChartScrollableGridDividerComponent(spec = spec)
    } else {
        ChartFixedGridDividerComponent(spec = spec)
    }
}

@Composable
private fun ChartScope.ChartFixedGridDividerComponent(
    spec: GridDividerSpec = GridDividerSpec()
) {
    Canvas(
        modifier = Modifier
            .fillMaxHeight()
            .anchor(anchor = ChartAnchor.Center)
            .clipToBounds()
    ) {
        var crossAxisOffset = 0f
        val crossAxisItemSize = size.height / spec.rows
        for (row in 0..spec.rows) {
            drawLine(
                color = spec.color,
                start = Offset(0f, crossAxisOffset),
                end = Offset(size.width, crossAxisOffset),
                strokeWidth = spec.strokeWidth.toPx()
            )
            crossAxisOffset += crossAxisItemSize
        }
        var mainAxisOffset = 0f
        val mainAxisItemSize = size.width / spec.columns
        for (column in 0..spec.columns) {
            drawLine(
                color = spec.color,
                start = Offset(mainAxisOffset, 0f),
                end = Offset(mainAxisOffset, size.height),
                strokeWidth = spec.strokeWidth.toPx()
            )
            mainAxisOffset += mainAxisItemSize
        }
    }
}

@Composable
private fun ChartScope.ChartScrollableGridDividerComponent(
    spec: GridDividerSpec = GridDividerSpec()
) {
    Canvas(
        modifier = Modifier
            .clipToBounds()
            .fillMaxHeight()
            .anchor(anchor = ChartAnchor.Center)
    ) {
        if (isHorizontal) {
            horizontalScrollableGridDividerComponent(
                chartScope = this@ChartScrollableGridDividerComponent,
                fixedDividerCount = spec.columns,
                dividerColor = spec.color,
                strokeWidth = spec.strokeWidth
            )
        } else {
            verticalScrollableGridDividerComponent(
                chartScope = this@ChartScrollableGridDividerComponent,
                fixedDividerCount = spec.rows,
                dividerColor = spec.color,
                strokeWidth = spec.strokeWidth
            )
        }
    }
}

private fun DrawScope.horizontalScrollableGridDividerComponent(
    chartScope: ChartScope,
    fixedDividerCount: Int = 4,
    dividerColor: Color = Color.LightGray,
    strokeWidth: Dp
) {
    var crossAxisOffset = 0f
    val crossAxisItemSize = size.height / (fixedDividerCount + 1)
    for (row in 0..fixedDividerCount) {
        drawLine(
            color = dividerColor,
            start = Offset(0f, crossAxisOffset),
            end = Offset(size.width, crossAxisOffset),
            strokeWidth = strokeWidth.toPx()
        )
        crossAxisOffset += crossAxisItemSize
    }
    with(chartScope) {
        val chartScrollState = chartContext.requireChartScrollState
        var itemOffset =
            -chartScrollState.firstVisibleItemOffset - chartGroupDivider / 2 - chartChildDivider / 2
        chartScrollState.currentVisibleRange.forEach { _ ->
            drawLine(
                color = dividerColor,
                start = Offset(itemOffset, 0f),
                end = Offset(itemOffset, size.height),
                strokeWidth = strokeWidth.toPx()
            )
            itemOffset += chartGroupOffsets
        }
    }
}

private fun DrawScope.verticalScrollableGridDividerComponent(
    chartScope: ChartScope,
    fixedDividerCount: Int = 4,
    dividerColor: Color = Color.LightGray,
    strokeWidth: Dp
) {
    with(chartScope) {
        val chartScrollState = chartContext.requireChartScrollState
        var itemOffset =
            -chartScrollState.firstVisibleItemOffset - chartGroupDivider / 2 - chartChildDivider / 2
        chartScrollState.currentVisibleRange.forEach { _ ->
            drawLine(
                color = dividerColor,
                start = Offset(0f, itemOffset),
                end = Offset(size.width, itemOffset),
                strokeWidth = strokeWidth.toPx()
            )
            itemOffset += chartGroupOffsets
        }
    }
    val mainAxisItemSize = size.width / fixedDividerCount
    var mainAxisOffset = 0f
    for (i in 0..fixedDividerCount) {
        drawLine(
            color = dividerColor,
            start = Offset(mainAxisOffset, 0f),
            end = Offset(mainAxisOffset, size.height),
            strokeWidth = strokeWidth.toPx()
        )
        mainAxisOffset += mainAxisItemSize
    }
}

/**
 * Specification for ChartBorderComponent
 */
class IndicationSpec(
    val backgroundColor: Color = Color.LightGray,
    val textColor: Color = Color.Black,
    val textSize: TextUnit = 16.sp,
    val size: Dp = 32.dp
)

@Composable
fun ChartScope.ChartIndicatorComponent(
    spec: IndicationSpec = IndicationSpec()
) {
    val scrollState = chartContext.requireChartScrollState
    val groupMainAxis = chartGroupOffsets
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = Modifier
            .clipToBounds()
            .background(color = spec.backgroundColor)
            .chartMainAxisIndicator(this, spec.size)
    ) {
        var textOffset = -scrollState.firstVisibleItemOffset
        for (index in scrollState.firstVisibleItem..scrollState.lastVisibleItem) {
            val textLayoutResult = textMeasurer.measure(
                text = (index + 1).toString(),
                style = TextStyle(color = spec.textColor, fontSize = spec.textSize)
            )
            if (isHorizontal) {
                drawText(
                    textLayoutResult = textLayoutResult, topLeft = Offset(
                        textOffset + (groupMainAxis - textLayoutResult.size.width) / 2f,
                        (size.height - textLayoutResult.size.height) / 2f
                    )
                )
            } else {
                drawText(
                    textLayoutResult = textLayoutResult, topLeft = Offset(
                        (size.width - textLayoutResult.size.width) / 2f,
                        textOffset + (groupMainAxis - textLayoutResult.size.height) / 2f,
                    )
                )
            }
            textOffset += groupMainAxis
        }
    }
}

fun Modifier.chartMainAxisIndicator(
    chartScope: ChartScope,
    size: Dp,
): Modifier = with(chartScope) {
    return if (isHorizontal) {
        anchor(ChartAnchor.Bottom)
            .fillMaxWidth()
            .height(size)
    } else {
        anchor(ChartAnchor.Start)
            .fillMaxHeight()
            .width(size)
    }
}

fun Modifier.chartCrossAxisSize(
    chartScope: ChartScope,
    size: Dp,
): Modifier = with(chartScope) {
    return if (isHorizontal) {
        anchor(ChartAnchor.Start)
            .fillMaxHeight()
            .width(size)
    } else {
        anchor(ChartAnchor.Bottom)
            .fillMaxWidth()
            .height(size)
    }
}

@Composable
fun ChartScope.ChartAverageAcrossRanksComponent(
    level: Int = 10,
    size: Dp = 32.dp,
    maxValueEvaluator: () -> Float
) {
    val maxValue = remember(maxValueEvaluator) {
        maxValueEvaluator()
    }
    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = Modifier
            .clipToBounds()
            .chartCrossAxisSize(this, size)
    ) {
        val textItemSize = this.size.maxDimension / level
        for (i in 0 until level) {
            val levelSize = (maxValue / level * (level - i))
            val textLayoutResult = textMeasurer.measure(
                text = levelSize.toInt().toString(),
                style = TextStyle(color = Color.Black, fontSize = 16.sp)
            )
            if (isHorizontal) {
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        (this.size.width - textLayoutResult.size.width) / 2f,
                        i * textItemSize + (textItemSize - textLayoutResult.size.height) / 2
                    )
                )
            } else {
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        (level - i - 1) * textItemSize + (textItemSize - textLayoutResult.size.width) / 2,
                        (this.size.height - textLayoutResult.size.height) / 2f,
                    )
                )
            }
        }
    }
}
