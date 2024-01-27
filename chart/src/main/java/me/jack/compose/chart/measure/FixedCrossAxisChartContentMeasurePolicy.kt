package me.jack.compose.chart.measure

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import me.jack.compose.chart.scope.ChartScope

class FixedCrossAxisChartContentMeasurePolicy(
    private val fixedCrossAxisSize: Float,
    private val childDividerSize: Float = 0f,
    private val groupDividerSize: Float = 0f
) : ChartContentMeasurePolicy {

    override val childSize: Size
        get() = Size(
            width = fixedCrossAxisSize,
            height = 0f
        )

    override val childDivider: Size
        get() = Size(
            width = childDividerSize,
            height = 0f
        )

    override val childOffsets: Size
        get() = Size(
            width = fixedCrossAxisSize + childDividerSize,
            height = 0f
        )

    override val groupDivider: Size
        get() = Size(width = groupDividerSize, height = 0f)

    override fun ChartScope.measureContent(size: IntSize): Size {
        return Size(
            width = (groupOffsets.width * childItemCount) - groupDividerSize - childDividerSize,
            height = size.height.toFloat()
        )
    }
}