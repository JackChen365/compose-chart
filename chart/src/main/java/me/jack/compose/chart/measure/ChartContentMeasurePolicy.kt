package me.jack.compose.chart.measure

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import me.jack.compose.chart.scope.ChartDatasetAccessScope
import me.jack.compose.chart.scope.ChartScope

fun fixedContentMeasurePolicy(
    fixedRowSize: Float,
    divideSize: Float = 0f,
    groupDividerSize: Float = 0f
): ChartContentMeasurePolicy {
    return FixedContentMeasurePolicy(fixedRowSize, divideSize, groupDividerSize)
}

fun fixedVerticalContentMeasurePolicy(
    fixedRowSize: Float,
    divideSize: Float = 0f,
    groupDividerSize: Float = 0f
): ChartContentMeasurePolicy {
    return FixedContentMeasurePolicy(fixedRowSize, divideSize, groupDividerSize, Orientation.Vertical)
}

fun fixedOverlayContentMeasurePolicy(
    fixedRowSize: Float,
    divideSize: Float = 0f,
    orientation: Orientation = Orientation.Horizontal
) = FixedOverlayContentMeasurePolicy(fixedRowSize, divideSize, orientation)


fun boxBoxChartContentMeasurePolicy() =
    BoxChartContentMeasurePolicy()

interface ChartContentMeasurePolicy {
    var contentSize: IntSize

    val ChartScope.groupSize: Float

    val childDividerSize: Float
        get() = 0f

    val groupDividerSize: Float
        get() = 0f

    val childSize: Size

    val orientation: Orientation
        get() = Orientation.Horizontal

    fun childLeftTop(
        groupCount: Int,
        groupIndex: Int,
        index: Int
    ): Offset

    fun ChartScope.measureContent(
        size: IntSize
    ): Size
}
