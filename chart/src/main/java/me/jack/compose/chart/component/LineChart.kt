package me.jack.compose.chart.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.jack.compose.chart.context.ChartContext
import me.jack.compose.chart.context.chartInteraction
import me.jack.compose.chart.context.scrollable
import me.jack.compose.chart.context.zoom
import me.jack.compose.chart.draw.ChartCanvas
import me.jack.compose.chart.draw.ChartDrawScope
import me.jack.compose.chart.draw.DrawElement
import me.jack.compose.chart.draw.interaction.pressInteractionState
import me.jack.compose.chart.interaction.asPressInteraction
import me.jack.compose.chart.measure.ChartContentMeasurePolicy
import me.jack.compose.chart.measure.fixedContentMeasurePolicy
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.LineData
import me.jack.compose.chart.model.maxOf
import me.jack.compose.chart.scope.LineChartScope
import me.jack.compose.chart.scope.SingleChartScope
import me.jack.compose.chart.scope.currentRange
import me.jack.compose.chart.scope.fastForEachWithNext
import me.jack.compose.chart.scope.isFirstIndex
import me.jack.compose.chart.scope.isHorizontal
import kotlin.math.max

private val DEFAULT_CROSS_AXIS_SIZE = 32.dp

class LineSpec(
    val strokeWidth: Dp = 4.dp,
    val circleRadius: Dp = 8.dp,
    val pressAlpha: Float = 0.4f
)

class CurveLineSpec(
    val strokeWidth: Dp = 4.dp,
    val circleRadius: Dp = 8.dp,
    val pressAlpha: Float = 0.4f,
    val style: DrawStyle = Fill
)

val lineChartContent: @Composable SingleChartScope<LineData>.() -> Unit = {
    ChartBorderComponent()
    ChartGridDividerComponent()
    ChartAverageAcrossRanksComponent { chartDataset.maxOf { it.value } }
    ChartIndicatorComponent()
    ChartContent()
}

val curveLineChartContent: @Composable LineChartScope.() -> Unit = {
    ChartBorderComponent()
    ChartGridDividerComponent()
    ChartAverageAcrossRanksComponent { chartDataset.maxOf { it.value } }
    ChartIndicatorComponent()
    ChartContent()
}

@Composable
fun SimpleLineChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<LineData>,
    lineSpec: LineSpec = LineSpec(),
    contentMeasurePolicy: ChartContentMeasurePolicy = fixedContentMeasurePolicy(DEFAULT_CROSS_AXIS_SIZE.toPx()),
    tapGestures: TapGestures<LineData> = TapGestures(),
    content: @Composable SingleChartScope<LineData>.() -> Unit = simpleChartContent
) {
    LineChart(
        modifier = modifier,
        chartDataset = chartDataset,
        lineSpec = lineSpec,
        contentMeasurePolicy = contentMeasurePolicy,
        tapGestures = tapGestures,
        content = content
    )
}

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<LineData>,
    lineSpec: LineSpec = LineSpec(),
    contentMeasurePolicy: ChartContentMeasurePolicy = fixedContentMeasurePolicy(DEFAULT_CROSS_AXIS_SIZE.toPx()),
    tapGestures: TapGestures<LineData> = TapGestures(),
    content: @Composable SingleChartScope<LineData>.() -> Unit = lineChartContent
) {
    SingleChartLayout(
        modifier = modifier,
        chartDataset = chartDataset,
        tapGestures = tapGestures,
        contentMeasurePolicy = contentMeasurePolicy,
        chartContext = ChartContext.chartInteraction(remember { MutableInteractionSource() })
            .scrollable(orientation = contentMeasurePolicy.orientation, state = rememberScrollState())
            .zoom(),
        content = content
    ) {
        ChartLineComponent(lineSpec = lineSpec)
        LineMarkerComponent()
    }
}

@Composable
fun SingleChartScope<LineData>.LineMarkerComponent() {
    val pressInteraction = chartContext.pressInteractionState.value.asPressInteraction<LineData>() ?: return
    val currentGroupItems = pressInteraction.currentGroupItems
    val drawElement = pressInteraction.drawElement
    if (drawElement is DrawElement.Rect) {
        MarkerDashLineComponent(
            leftTop = drawElement.topLeft,
            contentSize = drawElement.size,
            focusPoint = drawElement.focusPoint
        )
        MarkerComponent(
            leftTop = drawElement.topLeft,
            size = drawElement.size,
            focusPoint = drawElement.focusPoint,
            displayInfo = "(" + currentGroupItems.joinToString { it.value.toString() } + ")"
        )
    }
}

@Composable
fun SingleChartScope<LineData>.ChartLineComponent(
    lineSpec: LineSpec = LineSpec(),
) {
    val maxValue = remember(chartDataset) {
        chartDataset.maxOf { it.value }
    }
    ChartCanvas(
        modifier = Modifier.fillMaxSize()
    ) {
        horizontalLineChart(maxValue, lineSpec)
    }
}

private fun ChartDrawScope<LineData>.horizontalLineChart(
    maxValue: Float,
    lineSpec: LineSpec
) = with(singleChartScope) {
    val lineItemSize = size.crossAxis / maxValue
    val circleRadiusPx = lineSpec.circleRadius.toPx()
    fastForEachWithNext { current, next ->
        drawLine(
            color = current.color,
            start = if (isHorizontal) Offset(
                x = childCenterOffset.x,
                y = size.height - current.value * lineItemSize
            ) else Offset(
                x = size.width - current.value * lineItemSize,
                y = childCenterOffset.y
            ),
            end = if (isHorizontal) Offset(
                x = nextChildCenterOffset.x,
                y = size.height - next.value * lineItemSize
            ) else Offset(
                x = size.width - next.value * lineItemSize,
                y = 0f
            ),
            strokeWidth = lineSpec.strokeWidth.toPx()
        )
        if (isFirstIndex()) {
            clickableRect(
                topLeft = currentLeftTopOffset,
                size = childSize,
                focusPoint = Offset(
                    x = currentLeftTopOffset.x,
                    y = size.height - current.value * lineItemSize,
                )
            )
            drawCircle(
                color = current.color whenPressedAnimateTo current.color.copy(alpha = lineSpec.pressAlpha),
                radius = circleRadiusPx whenPressedAnimateTo circleRadiusPx * 1.4f,
                center = Offset(
                    x = childCenterOffset.x,
                    y = size.height - current.value * lineItemSize,
                )
            )
        }
        clickableRect(
            topLeft = nextLeftTopOffset,
            size = childSize,
            focusPoint = Offset(
                x = nextLeftTopOffset.x + childSize.mainAxis / 2,
                y = nextLeftTopOffset.y + childSize.crossAxis / 2
            )
        )
        drawCircle(
            color = current.color whenPressedAnimateTo current.color.copy(alpha = lineSpec.pressAlpha),
            radius = circleRadiusPx whenPressedAnimateTo circleRadiusPx * 1.4f,
            center = Offset(
                x = nextChildCenterOffset.x,
                y = size.height - next.value * lineItemSize,
            )
        )
    }
}

@Composable
fun SimpleCurveLineChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<LineData>,
    lineSpec: CurveLineSpec = CurveLineSpec(),
    contentMeasurePolicy: ChartContentMeasurePolicy = fixedContentMeasurePolicy(DEFAULT_CROSS_AXIS_SIZE.toPx()),
    tapGestures: TapGestures<LineData> = TapGestures(),
    content: @Composable SingleChartScope<LineData>.() -> Unit = simpleChartContent
) {
    SingleChartLayout(
        modifier = modifier,
        chartDataset = chartDataset,
        tapGestures = tapGestures,
        contentMeasurePolicy = contentMeasurePolicy,
        chartContext = ChartContext.chartInteraction(remember { MutableInteractionSource() })
            .scrollable(orientation = contentMeasurePolicy.orientation, state = rememberScrollState())
            .zoom(),
        content = { content() }
    ) {
        CurveLineComponent(lineSpec)
        LineMarkerComponent()
    }
}

@Composable
fun CurveLineChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<LineData>,
    lineSpec: CurveLineSpec = CurveLineSpec(),
    contentMeasurePolicy: ChartContentMeasurePolicy = fixedContentMeasurePolicy(DEFAULT_CROSS_AXIS_SIZE.toPx()),
    tapGestures: TapGestures<LineData> = TapGestures(),
    content: @Composable LineChartScope.() -> Unit = curveLineChartContent
) {
    SingleChartLayout(
        modifier = modifier,
        chartDataset = chartDataset,
        tapGestures = tapGestures,
        contentMeasurePolicy = contentMeasurePolicy,
        chartContext = ChartContext.chartInteraction(remember { MutableInteractionSource() })
            .scrollable(
                orientation = contentMeasurePolicy.orientation,
                state = rememberScrollState()
            )
            .zoom(),
        content = content,
        chartContent = {
            CurveLineComponent(lineSpec)
            LineMarkerComponent()
        }
    )
}

@Composable
fun SingleChartScope<LineData>.CurveLineComponent(
    lineSpec: CurveLineSpec = CurveLineSpec(),
) {
    HorizontalCurveLine(lineSpec)
}

@Composable
private fun SingleChartScope<LineData>.HorizontalCurveLine(spec: CurveLineSpec) {
    val path = remember { Path() }
    val maxValue = remember(chartDataset) {
        chartDataset.maxOf { it.value }
    }
    ChartCanvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val range = currentRange
        val strokeWidthPx = spec.strokeWidth.toPx()
        val lineItemSize = size.crossAxis / maxValue
        val start = max(0, range.first - 1)
        val end = range.last + 1
        fastForEachWithNext(start, end) { current, next ->
            val currentOffset = childCenterOffset.copy(y = size.height - current.value * lineItemSize)
            val nextOffset = nextChildCenterOffset.copy(y = size.height - next.value * lineItemSize)
            val firstControlPoint = Offset(
                x = currentOffset.x + (nextOffset.x - currentOffset.x) / 2F,
                y = currentOffset.y,
            )
            val secondControlPoint = Offset(
                x = currentOffset.x + (nextOffset.x - currentOffset.x) / 2F,
                y = nextOffset.y,
            )
            if (0 == index) {
                path.reset()
                path.moveTo(currentOffset.x - childOffsets.mainAxis, currentOffset.y)
                path.lineTo(currentOffset.x, currentOffset.y)
            } else if (index == start) {
                path.reset()
                path.moveTo(currentOffset.x, size.height)
                path.lineTo(currentOffset.x, currentOffset.y)
            }
            path.cubicTo(
                x1 = firstControlPoint.x,
                y1 = firstControlPoint.y,
                x2 = secondControlPoint.x,
                y2 = secondControlPoint.y,
                x3 = nextOffset.x,
                y3 = nextOffset.y,
            )
            // add clickable rect
            clickableRect(
                topLeft = currentLeftTopOffset,
                size = childSize,
                focusPoint = currentOffset
            )
            drawCircle(
                color = current.color whenPressedAnimateTo current.color.copy(alpha = spec.pressAlpha),
                radius = 0f whenPressedAnimateTo spec.circleRadius.toPx(),
                center = currentOffset
            )
            // add clickable rect
            clickableRect(
                topLeft = currentLeftTopOffset,
                size = Size(width = childSize.width, size.height),
                focusPoint = childCenterOffset.copy(y = size.height - current.value * lineItemSize)
            )
            if (index + 1 == range.last) {
                clickableRect(
                    topLeft = nextLeftTopOffset,
                    size = Size(width = childSize.width, size.height),
                    focusPoint = nextChildCenterOffset.copy(y = next.value * lineItemSize)
                )
                drawCircle(
                    color = next.color whenPressedAnimateTo next.color.copy(alpha = spec.pressAlpha),
                    radius = 0f whenPressedAnimateTo spec.circleRadius.toPx(),
                    center = nextChildCenterOffset.copy(y = next.value * lineItemSize)
                )
                path.lineTo(nextOffset.x + childSize.mainAxis / 2 + strokeWidthPx, nextOffset.y)
                path.lineTo(nextOffset.x + childSize.mainAxis / 2 + strokeWidthPx, size.height + strokeWidthPx)
                val currentItem: LineData = currentItem()
                drawPath(
                    path = path,
                    color = currentItem.color,
                    style = Stroke(strokeWidthPx),
                )
                path.lineTo(nextOffset.x + childSize.mainAxis / 2, size.height)
                path.lineTo(0f, size.height)
                drawPath(
                    path = path,
                    color = currentItem.color.copy(alpha = spec.pressAlpha),
                    style = spec.style
                )
            }
        }
    }
}