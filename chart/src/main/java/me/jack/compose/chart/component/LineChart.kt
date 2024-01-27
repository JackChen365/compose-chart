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
import me.jack.compose.chart.context.requireChartScrollState
import me.jack.compose.chart.context.scrollable
import me.jack.compose.chart.context.zoom
import me.jack.compose.chart.draw.ChartCanvas
import me.jack.compose.chart.draw.ChartDrawScope
import me.jack.compose.chart.draw.DrawElement
import me.jack.compose.chart.draw.interaction.pressInteractionState
import me.jack.compose.chart.interaction.asPressInteraction
import me.jack.compose.chart.measure.ChartContentMeasurePolicy
import me.jack.compose.chart.measure.fixedCrossAxisContentMeasurePolicy
import me.jack.compose.chart.measure.fixedMainAxisContentMeasurePolicy
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.LineData
import me.jack.compose.chart.model.forEachGroup
import me.jack.compose.chart.model.forEachGroupIndexed
import me.jack.compose.chart.model.forEachWithNext
import me.jack.compose.chart.model.maxOf
import me.jack.compose.chart.scope.SingleChartScope
import me.jack.compose.chart.scope.chartChildOffsets
import me.jack.compose.chart.scope.chartChildSize
import me.jack.compose.chart.scope.chartGroupOffsets
import me.jack.compose.chart.scope.getChartGroupOffsets
import me.jack.compose.chart.scope.isHorizontal

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

@Composable
fun SimpleLineChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<LineData>,
    lineSpec: LineSpec = LineSpec(),
    contentMeasurePolicy: ChartContentMeasurePolicy = fixedCrossAxisContentMeasurePolicy(DEFAULT_CROSS_AXIS_SIZE.toPx()),
    tapGestures: TapGestures<LineData> = TapGestures(),
    content: @Composable SingleChartScope<LineData>.() -> Unit = { ChartContent() }
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
    contentMeasurePolicy: ChartContentMeasurePolicy = fixedCrossAxisContentMeasurePolicy(DEFAULT_CROSS_AXIS_SIZE.toPx()),
    tapGestures: TapGestures<LineData> = TapGestures(),
    content: @Composable SingleChartScope<LineData>.() -> Unit = {
        ChartBorderComponent()
        ChartGridDividerComponent()
        ChartAverageAcrossRanksComponent { chartDataset.maxOf { it.value } }
        ChartIndicatorComponent()
        ChartContent()
    }
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
        if (isHorizontal) {
            horizontalLineChart(maxValue, lineSpec)
        } else {
            verticalLineChart(maxValue, lineSpec)
        }
    }
}

private fun ChartDrawScope<LineData>.horizontalLineChart(
    maxValue: Float,
    lineSpec: LineSpec,
) = with(singleChartScope) {
    val scrollState = chartContext.requireChartScrollState
    val lineItemSize = size.crossAxis / maxValue
    chartDataset.forEachGroupIndexed { groupIndex, groupName ->
        var offset: Float = -scrollState.firstVisibleItemOffset + getChartGroupOffsets(groupIndex).mainAxis
        chartDataset.forEachWithNext(
            chartGroup = groupName,
            start = scrollState.firstVisibleItem,
            end = scrollState.lastVisibleItem
        ) { current, next ->
            clickableRect(
                topLeft = Offset(offset, 0f),
                size = Size(chartChildSize.mainAxis, size.height),
                focusPoint = Offset(
                    x = offset + singleChartScope.chartChildOffsets.mainAxis / 2,
                    y = size.height - current.value * lineItemSize
                )
            )
            drawLine(
                color = current.color,
                start = Offset(
                    x = offset + singleChartScope.chartChildOffsets.mainAxis / 2,
                    y = size.height - current.value * lineItemSize
                ),
                end = Offset(
                    x = offset + singleChartScope.chartChildOffsets.mainAxis / 2 + singleChartScope.chartGroupOffsets.mainAxis,
                    y = size.height - next.value * lineItemSize
                ),
                strokeWidth = lineSpec.strokeWidth.toPx()
            )
            val circleRadiusPx = lineSpec.circleRadius.toPx()
            drawCircle(
                color = current.color whenPressedAnimateTo current.color.copy(alpha = lineSpec.pressAlpha),
                radius = circleRadiusPx whenPressedAnimateTo circleRadiusPx * 1.4f,
                center = Offset(
                    x = offset + singleChartScope.chartChildOffsets.mainAxis / 2,
                    y = size.height - current.value * lineItemSize
                )
            )
            offset += singleChartScope.chartGroupOffsets.mainAxis
            // draw the last circle
            if (index + 1 == scrollState.lastVisibleItem - 1) {
                clickableRect(
                    topLeft = Offset(offset, 0f),
                    size = Size(chartChildSize.mainAxis, size.height),
                    currentItem = next,
                    index = index + 1,
                    focusPoint = Offset(
                        x = offset + singleChartScope.chartChildOffsets.mainAxis / 2,
                        y = size.height - next.value * lineItemSize
                    )
                )
                drawCircle(
                    color = current.color whenPressedAnimateTo current.color.copy(alpha = lineSpec.pressAlpha),
                    radius = circleRadiusPx whenPressedAnimateTo circleRadiusPx * 1.4f,
                    center = Offset(
                        x = offset + singleChartScope.chartChildOffsets.mainAxis / 2,
                        y = size.height - next.value * lineItemSize
                    )
                )
            }
        }
    }
}

private fun ChartDrawScope<LineData>.verticalLineChart(
    maxValue: Float,
    lineSpec: LineSpec
) = with(singleChartScope) {
    val lineItemSize = size.crossAxis / maxValue
    chartDataset.forEachGroupIndexed { groupIndex, groupName ->
        val scrollState = chartContext.requireChartScrollState
        var offset: Float = -scrollState.firstVisibleItemOffset + getChartGroupOffsets(groupIndex).mainAxis
        chartDataset.forEachWithNext(
            chartGroup = groupName,
            start = scrollState.firstVisibleItem,
            end = scrollState.lastVisibleItem
        ) { current, next ->
            clickableRect(
                topLeft = Offset(0f, offset),
                size = Size(size.width, chartChildSize.mainAxis),
                focusPoint = Offset(
                    x = current.value * lineItemSize,
                    y = offset + singleChartScope.chartChildOffsets.mainAxis / 2
                )
            )
            drawLine(
                color = current.color,
                start = Offset(
                    x = current.value * lineItemSize,
                    y = offset + singleChartScope.chartChildOffsets.mainAxis / 2
                ),
                end = Offset(
                    x = next.value * lineItemSize,
                    y = offset + singleChartScope.chartChildOffsets.mainAxis / 2 + singleChartScope.chartGroupOffsets.mainAxis
                ),
                strokeWidth = lineSpec.strokeWidth.toPx()
            )
            val circleRadiusPx = lineSpec.circleRadius.toPx()
            drawCircle(
                color = current.color whenPressedAnimateTo current.color.copy(alpha = lineSpec.pressAlpha),
                radius = circleRadiusPx whenPressedAnimateTo circleRadiusPx * 1.4f,
                center = Offset(
                    x = current.value * lineItemSize,
                    y = offset + singleChartScope.chartChildOffsets.mainAxis / 2
                )
            )
            offset += singleChartScope.chartGroupOffsets.mainAxis
            // draw the last circle
            if (index + 1 == scrollState.lastVisibleItem - 1) {
                clickableRect(
                    topLeft = Offset(0f, offset),
                    size = Size(size.width, chartChildSize.mainAxis),
                    currentItem = next,
                    focusPoint = Offset(
                        x = next.value * lineItemSize,
                        y = offset + singleChartScope.chartChildOffsets.mainAxis / 2
                    ),
                    index = index + 1
                )
                drawCircle(
                    color = current.color whenPressedAnimateTo current.color.copy(alpha = lineSpec.pressAlpha),
                    radius = circleRadiusPx whenPressedAnimateTo circleRadiusPx * 1.4f,
                    center = Offset(
                        x = next.value * lineItemSize,
                        y = offset + singleChartScope.chartChildOffsets.mainAxis / 2
                    )
                )
            }
        }
    }
}

@Composable
fun SimpleCurveLineChart(
    modifier: Modifier = Modifier,
    chartDataset: ChartDataset<LineData>,
    lineSpec: CurveLineSpec = CurveLineSpec(),
    contentMeasurePolicy: ChartContentMeasurePolicy = fixedMainAxisContentMeasurePolicy(DEFAULT_CROSS_AXIS_SIZE.toPx()),
    tapGestures: TapGestures<LineData> = TapGestures(),
    content: @Composable SingleChartScope<LineData>.() -> Unit = { ChartContent() }
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
    contentMeasurePolicy: ChartContentMeasurePolicy = fixedMainAxisContentMeasurePolicy(DEFAULT_CROSS_AXIS_SIZE.toPx()),
    tapGestures: TapGestures<LineData> = TapGestures(),
    content: @Composable SingleChartScope<LineData>.() -> Unit = {
        ChartBorderComponent()
        ChartGridDividerComponent()
        ChartAverageAcrossRanksComponent { chartDataset.maxOf { it.value } }
        ChartIndicatorComponent()
        ChartContent()
    }
) {
    SingleChartLayout(
        modifier = modifier,
        chartDataset = chartDataset,
        tapGestures = tapGestures,
        contentMeasurePolicy = contentMeasurePolicy,
        chartContext = ChartContext.chartInteraction(remember { MutableInteractionSource() })
            .scrollable(orientation = contentMeasurePolicy.orientation, state = rememberScrollState())
            .zoom(),
        content = { content() },
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
    if (isHorizontal) {
        HorizontalCurveLine(lineSpec)
    } else {
        error("Does not support VerticalCurveLine.")
    }
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
        val lineItemSize = size.crossAxis / maxValue
        val scrollState = chartContext.requireChartScrollState
        chartDataset.forEachGroup { groupName ->
            path.reset()
            var lastOffset: Offset = Offset.Zero
            var firstVisibleItem = scrollState.firstVisibleItem
            val lastVisibleItem = scrollState.lastVisibleItem
            var offset: Float = -scrollState.firstVisibleItemOffset
            if (0 < scrollState.firstVisibleItem) {
                firstVisibleItem -= 1
                offset = -scrollState.firstVisibleItemOffset - chartChildOffsets.mainAxis
            }
            chartDataset.forEachWithNext(
                chartGroup = groupName,
                start = firstVisibleItem,
                end = lastVisibleItem
            ) { current, next ->
                val currentOffset = Offset(
                    x = offset + chartChildOffsets.mainAxis / 2,
                    y = size.height - current.value * lineItemSize
                )
                val nextOffset = Offset(
                    x = currentOffset.x + chartChildOffsets.mainAxis,
                    y = size.height - next.value * lineItemSize
                )
                val firstControlPoint = Offset(
                    x = currentOffset.x + (nextOffset.x - currentOffset.x) / 2F,
                    y = currentOffset.y,
                )
                val secondControlPoint = Offset(
                    x = currentOffset.x + (nextOffset.x - currentOffset.x) / 2F,
                    y = nextOffset.y,
                )
                if (0 == index) {
                    path.moveTo(currentOffset.x - chartChildOffsets.mainAxis, currentOffset.y)
                    path.lineTo(currentOffset.x, currentOffset.y)
                } else if (index == firstVisibleItem) {
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
                    topLeft = Offset(
                        x = offset,
                        y = 0f
                    ),
                    size = Size(width = chartChildOffsets.width, size.height),
                    focusPoint = currentOffset
                )
                drawCircle(
                    color = current.color whenPressedAnimateTo current.color.copy(alpha = spec.pressAlpha),
                    radius = 0f whenPressedAnimateTo spec.circleRadius.toPx(),
                    center = currentOffset
                )
                offset += chartChildOffsets.mainAxis
                if (index + 1 == scrollState.lastVisibleItem - 1) {
                    path.lineTo(nextOffset.x + chartChildOffsets.mainAxis / 2, nextOffset.y)
                    // add clickable rect
                    clickableRect(
                        topLeft = Offset(
                            x = nextOffset.x - chartChildOffsets.width / 2,
                            y = 0f
                        ),
                        size = Size(width = chartChildOffsets.width, size.height),
                        currentItem = next,
                        focusPoint = Offset(
                            x = offset + chartChildOffsets.mainAxis / 2,
                            y = size.height - next.value * lineItemSize
                        ),
                        index = index + 1
                    )
                    drawCircle(
                        color = next.color whenPressedAnimateTo next.color.copy(alpha = spec.pressAlpha),
                        radius = 0f whenPressedAnimateTo spec.circleRadius.toPx(),
                        center = Offset(
                            x = offset + chartChildOffsets.mainAxis / 2,
                            y = size.height - next.value * lineItemSize
                        )
                    )
                    lastOffset = nextOffset
                }
            }
            // draw stroke path
            val lineData = chartDataset[groupName].first()
            drawPath(
                path = path,
                color = lineData.color,
                style = Stroke(spec.strokeWidth.toPx()),
            )
            path.lineTo(lastOffset.x + chartChildOffsets.mainAxis / 2, size.height)
            path.lineTo(0f, size.height)
            drawPath(
                path = path,
                color = lineData.color.copy(alpha = spec.pressAlpha),
                style = spec.style
            )
        }
    }
}