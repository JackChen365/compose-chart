package me.jack.compose.chart.measure

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import me.jack.compose.chart.scope.ChartScope

fun fixedMainAxisContentMeasurePolicy(
    fixedColumnSize: Float,
    divideSize: Float = 0f,
    groupDividerSize: Float = 0f
): ChartContentMeasurePolicy {
    return FixedMainAxisAxisChartContentMeasurePolicy(fixedColumnSize, divideSize, groupDividerSize)
}

fun fixedCrossAxisContentMeasurePolicy(
    fixedRowSize: Float,
    divideSize: Float = 0f,
    groupDividerSize: Float = 0f
): ChartContentMeasurePolicy {
    return FixedCrossAxisChartContentMeasurePolicy(fixedRowSize, divideSize, groupDividerSize)
}

fun fixedCrossAxisOverlayContentMeasurePolicy(
    fixedRowSize: Float,
    divideSize: Float = 0f
) = FixedCrossAxisOverlayContentMeasurePolicy(fixedRowSize, divideSize)


fun fixedMainAxisOverlayContentMeasurePolicy(
    fixedColumnSize: Float,
    divideSize: Float = 0f
) = FixedMainAxisOverlayContentMeasurePolicy(fixedColumnSize, divideSize)


fun boxBoxChartContentMeasurePolicy(childSize: Size = Size.Zero) =
    BoxChartContentMeasurePolicy(childSize = childSize)

interface ChartContentMeasurePolicy {
    fun ChartScope.measureContent(
        size: IntSize
    ): Size

    val childSize: Size

    val childOffsets: Size
        get() = childSize

    val childDivider: Size
        get() = Size.Zero

    val groupDivider: Size
        get() = Size.Zero

    val ChartScope.groupOffsets: Size
        get() = Size(
            width = groupCount * childOffsets.mainAxis + groupDivider.mainAxis,
            height = groupCount * childOffsets.mainAxis + groupDivider.mainAxis
        )

    fun getGroupOffsets(index: Int): Size {
        return Size(
            width = index * childOffsets.width,
            height = index * childOffsets.height
        )
    }

    val orientation: Orientation
        get() = Orientation.Horizontal
}
