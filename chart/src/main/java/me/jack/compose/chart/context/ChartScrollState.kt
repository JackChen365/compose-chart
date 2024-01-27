package me.jack.compose.chart.context

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

val ChartContext.requireChartScrollState: ChartScrollState
    get() = chartScrollState ?: error("Can not found the chart scroll state.")


val ChartContext.chartScrollState: ChartScrollState?
    get() = get(ChartScrollState)

fun ChartContext.scrollable(
    orientation: Orientation = Orientation.Horizontal, state: ScrollableState
): ChartContext {
    return this + MutableChartScrollState(state, orientation)
}

internal class MutableChartScrollState(
    override val state: ScrollableState, override val orientation: Orientation
) : ChartScrollState {
    private var firstVisibleItemState = mutableIntStateOf(0)
    private var firstVisibleItemOffsetState = mutableFloatStateOf(0f)
    private var lastVisibleItemState = mutableIntStateOf(0)
    override var offset: Float by mutableFloatStateOf(0f)
    override var firstVisibleItem: Int
        set(value) {
            firstVisibleItemState.intValue = value
        }
        get() = firstVisibleItemState.intValue


    override var firstVisibleItemOffset: Float
        set(value) {
            firstVisibleItemOffsetState.floatValue = value
        }
        get() = firstVisibleItemOffsetState.floatValue

    override var lastVisibleItem: Int
        set(value) {
            lastVisibleItemState.intValue = value
        }
        get() = lastVisibleItemState.intValue
}

val Orientation.isHorizontal: Boolean
    get() = this == Orientation.Horizontal

interface ChartScrollState : ChartContext.Element {
    companion object Key : ChartContext.Key<ChartScrollState>

    override val key: ChartContext.Key<*> get() = Key

    val state: ScrollableState
    val orientation: Orientation

    var offset: Float
    val firstVisibleItem: Int
    val firstVisibleItemOffset: Float
    val lastVisibleItem: Int

    val currentVisibleRange: IntRange
        get() = firstVisibleItem until lastVisibleItem
}