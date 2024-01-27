package me.jack.compose.chart.scope

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ParentDataModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.context.ChartContext
import me.jack.compose.chart.context.ChartScrollState
import me.jack.compose.chart.context.isHorizontal
import me.jack.compose.chart.context.requireChartScrollState
import me.jack.compose.chart.measure.ChartContentMeasurePolicy
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.model.forEach
import me.jack.compose.chart.model.forEachGroup
import me.jack.compose.chart.model.forEachGroupIndexed
import me.jack.compose.chart.model.maxOf
import me.jack.compose.chart.model.minOf
import java.util.Objects
import kotlin.math.max
import kotlin.math.min

enum class ChartAnchor {
    Start, Top, End, Bottom, Center
}

internal val IntrinsicMeasurable.chartParentData: ChartParentData?
    get() = parentData as? ChartParentData

internal val ChartParentData?.anchor: ChartAnchor
    get() = this?.anchor ?: ChartAnchor.Center

internal val Placeable?.anchor: ChartAnchor
    get() = (this?.parentData as? ChartParentData)?.anchor ?: ChartAnchor.Center

internal val Placeable?.alignContent: Boolean
    get() = (this?.parentData as? ChartParentData)?.alignContent ?: true

/**
 * Parent data associated with children.
 */
internal data class ChartParentData(
    var anchor: ChartAnchor = ChartAnchor.Center, var alignContent: Boolean = true
)

interface MutableScrollableScope {
    var contentSize: Size
    var contentRange: Size
}

class SingleChartScopeInstance<T>(
    override val chartDataset: ChartDataset<T>,
    override val chartContext: ChartContext = ChartContext,
    override val tapGestures: TapGestures<T> = TapGestures(),
    override val contentMeasurePolicy: ChartContentMeasurePolicy,
) : SingleChartScope<T>, MutableScrollableScope {

    override var contentSize: Size = Size.Zero
    override var contentRange: Size = Size.Zero

    internal var chartContent: @Composable (SingleChartScope<T>.() -> Unit)? = null

    @Composable
    override fun ChartContent() {
        chartContent?.invoke(this)
    }

    override fun Modifier.anchor(anchor: ChartAnchor, alignContent: Boolean): Modifier {
        return this.then(ChartAnchorElement(anchor))
    }
}

class ChartScopeInstance(
    override val chartContext: ChartContext,
    override val contentMeasurePolicy: ChartContentMeasurePolicy,
    override val childItemCount: Int,
    override val groupCount: Int
) : ChartScope, MutableScrollableScope {
    override var contentSize: Size = Size.Zero
    override var contentRange: Size = Size.Zero

    override fun Modifier.anchor(anchor: ChartAnchor, alignContent: Boolean): Modifier {
        return this.then(ChartAnchorElement(anchor))
    }
}

val ChartScope.chartChildSize: Size
    get() = contentMeasurePolicy.childSize

val ChartScope.chartChildOffsets: Size
    get() = contentMeasurePolicy.childOffsets

val ChartScope.chartChildDivider: Size
    get() = contentMeasurePolicy.childDivider

val ChartScope.chartGroupDivider: Size
    get() = contentMeasurePolicy.groupDivider

val ChartScope.chartGroupOffsets: Size
    get() = with(contentMeasurePolicy) { groupOffsets }

fun ChartScope.getChartGroupOffsets(index: Int): Size =
    contentMeasurePolicy.getGroupOffsets(index)

val ChartScope.isHorizontal: Boolean
    get() = contentMeasurePolicy.orientation.isHorizontal


@Immutable
// @LayoutScopeMarker remove it due to access chartData in any scope
interface ChartScope {
    val chartContext: ChartContext

    val contentMeasurePolicy: ChartContentMeasurePolicy

    val contentSize: Size

    val contentRange: Size

    val childItemCount: Int

    val groupCount: Int
        get() = 1

    val Size.mainAxis: Float
        get() = if (isHorizontal) width else height

    val Size.crossAxis: Float
        get() = if (isHorizontal) height else width

    val IntSize.mainAxis: Int
        get() = if (isHorizontal) width else height

    val IntSize.crossAxis: Int
        get() = if (isHorizontal) height else width

    @Stable
    fun Modifier.anchor(
        anchor: ChartAnchor, alignContent: Boolean = true
    ): Modifier
}

interface SingleChartScope<T> : ChartScope {
    val chartDataset: ChartDataset<T>
    val tapGestures: TapGestures<T>

    override val childItemCount: Int
        get() = chartDataset.size

    override val groupCount: Int
        get() = chartDataset.groupSize

    @Composable
    fun ChartContent()
}

internal class ChartAnchorNode(
    var anchor: ChartAnchor, var alignContent: Boolean = true
) : ParentDataModifierNode, Modifier.Node() {
    override fun Density.modifyParentData(parentData: Any?): ChartParentData {
        return ((parentData as? ChartParentData) ?: ChartParentData()).also {
            it.anchor = anchor
            it.alignContent = alignContent
        }
    }
}

internal class ChartAnchorElement(
    val anchor: ChartAnchor,
    var alignContent: Boolean = true
) : ModifierNodeElement<ChartAnchorNode>() {
    override fun create(): ChartAnchorNode {
        return ChartAnchorNode(anchor, alignContent)
    }

    override fun update(node: ChartAnchorNode) {
        node.anchor = anchor
        node.alignContent = alignContent
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "ChartAnchorElement"
        value = anchor
    }

    override fun hashCode(): Int = Objects.hash(anchor, alignContent)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? ChartAnchorNode ?: return false
        return anchor == otherModifier.anchor && alignContent == otherModifier.alignContent
    }
}

interface ChartDatasetAccessScope {
    val chartGroup: String
    val groupIndex: Int
    val index: Int
    fun <T> currentItem(): T
}

object SimpleChartDatasetAccessScope : ChartDatasetAccessScope {
    override val chartGroup: String
        get() = internalChartGroup ?: error("The current chart group is null.")
    override var groupIndex: Int = -1
    override var index: Int = -1
    var internalChartGroup: String? = null
    var internalCurrentItem: Any? = null
    override fun <T> currentItem(): T {
        @Suppress("UNCHECKED_CAST")
        return checkNotNull(internalCurrentItem) as T
    }
}

inline fun <T> SingleChartScope<T>.maxOf(action: (T) -> Float): Float {
    val scrollState = chartContext.requireChartScrollState
    var maxValue = Float.MIN_VALUE
    chartDataset.forEachGroup {
        maxValue = max(a = maxValue, b = chartDataset.maxOf(
            start = scrollState.firstVisibleItem, end = scrollState.lastVisibleItem
        ) { chartData ->
            action(chartData)
        })
    }
    return maxValue
}

inline fun <T> SingleChartScope<T>.minOf(action: (T) -> Float): Float {
    val scrollState = chartContext.requireChartScrollState
    var minValue = Float.MAX_VALUE
    chartDataset.forEachGroup {
        minValue = min(a = minValue, b = chartDataset.minOf(
            start = scrollState.firstVisibleItem, end = scrollState.lastVisibleItem
        ) { chartData ->
            action(chartData)
        })
    }
    return minValue
}

inline fun <T> SingleChartScope<T>.fastForEach(
    action: ChartDatasetAccessScope.(chartGroup: String, T) -> Unit
) {
    val scrollState = chartContext[ChartScrollState]
    if (null == scrollState) {
        chartDataset.forEachGroup { chartGroup ->
            chartDataset.forEach(
                chartGroup = chartGroup
            ) { currentItem ->
                action(chartGroup, currentItem)
            }
        }
    } else {
        chartDataset.forEachGroup { chartGroup ->
            chartDataset.forEach(
                chartGroup = chartGroup,
                start = scrollState.firstVisibleItem,
                end = scrollState.lastVisibleItem
            ) { currentItem ->
                action(chartGroup, currentItem)
            }
        }
    }
}