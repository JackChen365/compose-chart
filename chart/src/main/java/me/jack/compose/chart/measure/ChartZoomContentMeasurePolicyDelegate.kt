package me.jack.compose.chart.measure

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import me.jack.compose.chart.scope.ChartScope

fun ChartContentMeasurePolicy.asZoomableContentMeasurePolicy(
    onZoomValueChanged: () -> Float = { 1f }
): ChartContentMeasurePolicy {
    return ChartZoomContentMeasurePolicyDelegate(this, onZoomValueChanged)
}

class ChartZoomContentMeasurePolicyDelegate(
    private val measurePolicy: ChartContentMeasurePolicy,
    private val onZoomValueChanged: () -> Float
) : ChartContentMeasurePolicy by measurePolicy {
    override val childSize: Size
        get() {
            val childSize = measurePolicy.childSize
            return childSize.times(onZoomValueChanged())
        }

    override val childOffsets: Size
        get() {
            val childOffsets = measurePolicy.childOffsets
            return childOffsets.times(onZoomValueChanged())
        }

    override val groupDivider: Size
        get() {
            val groupInsets = measurePolicy.groupDivider
            return groupInsets.times(onZoomValueChanged())
        }

    override val ChartScope.groupOffsets: Size
        get() {
            val groupOffsets = with(measurePolicy) { groupOffsets }
            return groupOffsets.times(onZoomValueChanged())
        }

    override fun getGroupOffsets(index: Int): Size {
        return measurePolicy.getGroupOffsets(index).times(onZoomValueChanged())
    }

    override fun ChartScope.measureContent(size: IntSize): Size {
        val contentSize = with(measurePolicy) {
            measureContent(size)
        }
        return contentSize.times(onZoomValueChanged())
    }
}