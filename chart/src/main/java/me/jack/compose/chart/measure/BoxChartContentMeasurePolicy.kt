package me.jack.compose.chart.measure

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import me.jack.compose.chart.scope.ChartScope
import me.jack.compose.chart.scope.SingleChartScope

class BoxChartContentMeasurePolicy(
    override val childSize: Size
) : ChartContentMeasurePolicy {

    override val childOffsets: Size
        get() = Size.Zero

    override val groupDivider: Size
        get() = Size.Zero

    override fun ChartScope.measureContent(size: IntSize): Size {
        return size.toSize()
    }
}