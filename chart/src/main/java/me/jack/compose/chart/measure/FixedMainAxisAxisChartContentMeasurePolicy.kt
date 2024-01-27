package me.jack.compose.chart.measure

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import me.jack.compose.chart.scope.ChartScope

class FixedMainAxisAxisChartContentMeasurePolicy(
    private val fixedMainAxisSize: Float,
    private val childDividerSize: Float = 0f,
    private val groupDividerSize: Float = 0f
) : ChartContentMeasurePolicy {

    override val childSize: Size
        get() = Size(
            width = 0f,
            height = fixedMainAxisSize
        )

    override val childDivider: Size
        get() = Size(
            width = 0f,
            height = childDividerSize
        )

    override val childOffsets: Size
        get() = Size(
            width = 0f,
            height = fixedMainAxisSize + this.childDividerSize
        )

    override val groupDivider: Size
        get() = Size(
            width = 0f,
            height = groupDividerSize
        )

    override fun ChartScope.measureContent(size: IntSize): Size {
        return Size(
            width = size.width.toFloat(),
            height = ((childOffsets.height * groupCount + groupDividerSize) * childItemCount) - groupDividerSize - childDividerSize,
        )
    }

    override val orientation: Orientation
        get() = Orientation.Vertical
}