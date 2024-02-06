package me.jack.compose.chart.model

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import me.jack.compose.chart.animation.ChartAnimatableState
import me.jack.compose.chart.animation.ChartColorAnimatableState
import me.jack.compose.chart.animation.ChartFloatAnimatableState
import me.jack.compose.chart.animation.ChartIntAnimatableState
import me.jack.compose.chart.animation.colorAnimatableState
import me.jack.compose.chart.animation.floatAnimatableState
import me.jack.compose.chart.animation.intAnimatableState
import me.jack.compose.chart.scope.ChartDatasetAccessScope
import me.jack.compose.chart.scope.ChartDatasetAccessScopeInstance
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import kotlin.math.max
import kotlin.math.min

const val SINGLE_GROUP_NAME = "#"
fun <T> List<T>.asChartDataset(): ChartDataset<T> =
    MutableChartDataset<T>().also { it.addChartData(SINGLE_GROUP_NAME, this) }

fun <T> simpleChartDataset(): ChartDataset<T> {
    return MutableChartDataset()
}

fun <T> chartDataGroup(block: ChartDataGroupBuilder<T>.() -> Unit): ChartDataset<T> {
    val datasetBuilder = ChartDataGroupBuilder<T>()
    block.invoke(datasetBuilder)
    return datasetBuilder.getChartDataset()
}

@DslMarker
annotation class ChartDataGroupBuilderMarker

@ChartDataGroupBuilderMarker
class ChartDataGroupBuilder<T> {
    private val chartDataset: MutableChartDataset<T> = MutableChartDataset()

    fun dataset(chartGroup: String, block: DatasetBuilder<T>.() -> Unit) {
        val datasetBuilder = DatasetBuilder<T>()
        block.invoke(datasetBuilder)
        chartDataset.addChartData(chartGroup = chartGroup, chartData = datasetBuilder.getDataset())
    }

    fun animatableDataset(
        scope: CoroutineScope,
        chartGroup: String,
        block: DatasetBuilder<T>.() -> Unit
    ) {
        val datasetBuilder = DatasetBuilder<T>()
        block.invoke(datasetBuilder)
        val dataset = datasetBuilder.getDataset()
        val animatableItems = dataset.map { getAnimatableDelegateItem(scope, it) }
        chartDataset.addChartData(chartGroup = chartGroup, chartData = animatableItems)
    }

    @Suppress("UNCHECKED_CAST")
    private fun getAnimatableDelegateItem(scope: CoroutineScope, item: T): T {
        val itemClass = item!!::class.java
        val itemAnimatableFields = mutableMapOf<String, ChartAnimatableState<*, *>>()
        return Proxy.newProxyInstance(
            itemClass.classLoader,
            itemClass.interfaces,
            object : InvocationHandler {
                override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
                    val propertyName = resolvePropertyName(method.name)
                    if (method.name.startsWith("set")) {
                        val firstArgument = args?.firstOrNull()
                        checkNotNull(firstArgument)
                        val animatableState =
                            getPropertyAnimatableField(item, propertyName, firstArgument::class.java)
                        if (null != animatableState) {
                            setAnimatableFieldValue(animatableState, firstArgument)
                        }
                    } else if (method.name.startsWith("get")) {
                        val animatableState =
                            getPropertyAnimatableField(item, propertyName, method.returnType)
                        if (null != animatableState) {
                            return animatableState.value
                        }
                    }
                    return if (null != args) {
                        method.invoke(item, *args)
                    } else {
                        method.invoke(item)
                    }
                }

                private fun setAnimatableFieldValue(animatableState: ChartAnimatableState<*, *>, value: Any) {
                    when (animatableState) {
                        is ChartColorAnimatableState -> {
                            animatableState.value = value as Color
                        }

                        is ChartIntAnimatableState -> {
                            animatableState.value = value as Int
                        }

                        is ChartFloatAnimatableState -> {
                            animatableState.value = value as Float
                        }
                    }
                }

                private fun getPropertyAnimatableField(
                    item: T,
                    propertyName: String,
                    propertyType: Class<*>
                ): ChartAnimatableState<*, *>? {
                    val key = propertyName + System.identityHashCode(item)
                    var animatableState = itemAnimatableFields[key]
                    if (null == animatableState) {
                        animatableState = when (propertyType) {
                            Color::class.java -> {
                                colorAnimatableState(scope, readInstanceProperty(item as Any, propertyName))
                            }

                            Float::class.java -> {
                                floatAnimatableState(scope, readInstanceProperty(item as Any, propertyName))
                            }

                            Int::class.java -> {
                                intAnimatableState(scope, readInstanceProperty(item as Any, propertyName))
                            }

                            else -> null
                        }
                        if (null != animatableState) {
                            itemAnimatableFields[key] = animatableState
                        }
                    }
                    return animatableState
                }

                private fun resolvePropertyName(name: String): String {
                    return if (name.startsWith("set")) {
                        name.substringAfterLast("set")
                    } else if (name.startsWith("get")) {
                        name.substringAfterLast("get")
                    } else name
                }

                fun <R> readInstanceProperty(instance: Any, propertyName: String): R {
                    val getPropertyValueMethod = instance::class.java.getMethod("get$propertyName")
                    return getPropertyValueMethod.invoke(instance) as R
                }
            }
        ) as T
    }

    class DatasetBuilder<T> {
        private val internalList = mutableListOf<T>()
        fun items(itemCount: Int, block: (index: Int) -> T) {
            repeat(itemCount) { index ->
                internalList.add(block(index))
            }
        }

        fun <E> items(list: List<E>, block: (index: Int, item: E) -> T) {
            list.forEachIndexed { index, item ->
                internalList.add(block(index, item))
            }
        }

        fun getDataset() = internalList
    }

    fun getChartDataset(): ChartDataset<T> {
        return chartDataset
    }
}

inline fun ChartDataset<*>.forEachGroup(action: (chartGroup: String) -> Unit) {
    for (element in chartGroups) action(element)
}

inline fun ChartDataset<*>.forEachGroupIndexed(action: (index: Int, chartGroup: String) -> Unit) {
    var index = 0
    for (element in chartGroups) action(index++, element)
}

inline fun <T> ChartDataset<T>.forEach(
    action: ChartDatasetAccessScope.(chartGroup: String, T) -> Unit
) {
    forEachGroupIndexed { groupIndex, chartGroup ->
        ChartDatasetAccessScopeInstance.groupIndex = groupIndex
        ChartDatasetAccessScopeInstance.internalChartGroup = chartGroup
        this[chartGroup].forEachIndexed { index, currentItem ->
            ChartDatasetAccessScopeInstance.internalCurrentItem = currentItem
            ChartDatasetAccessScopeInstance.index = index
            ChartDatasetAccessScopeInstance.action(chartGroup, currentItem)
        }
    }
}

inline fun <T> ChartDataset<T>.forEach(
    chartGroup: String = SINGLE_GROUP_NAME,
    action: ChartDatasetAccessScope.(T) -> Unit
) {
    ChartDatasetAccessScopeInstance.internalChartGroup = chartGroup
    ChartDatasetAccessScopeInstance.groupIndex = chartGroups.indexOf(chartGroup)
    this[chartGroup].forEachIndexed { index, currentItem ->
        ChartDatasetAccessScopeInstance.internalCurrentItem = currentItem
        ChartDatasetAccessScopeInstance.index = index
        ChartDatasetAccessScopeInstance.action(currentItem)
    }
}

inline fun <T> ChartDataset<T>.forEach(
    chartGroup: String = SINGLE_GROUP_NAME,
    start: Int = 0,
    end: Int = Int.MAX_VALUE,
    action: ChartDatasetAccessScope.(T) -> Unit
) {
    val dataset = this[chartGroup]
    var index = start
    val dataSize = min(size, end)
    ChartDatasetAccessScopeInstance.internalChartGroup = chartGroup
    ChartDatasetAccessScopeInstance.groupIndex = chartGroups.indexOf(chartGroup)
    while (index < dataSize) {
        val currentItem = dataset[index]
        ChartDatasetAccessScopeInstance.internalCurrentItem = currentItem
        ChartDatasetAccessScopeInstance.index = index
        ChartDatasetAccessScopeInstance.action(currentItem)
        index++
    }
}

inline fun <T> ChartDataset<T>.forEachWithNext(
    chartGroup: String = SINGLE_GROUP_NAME,
    start: Int = 0,
    end: Int = Int.MAX_VALUE,
    action: ChartDatasetAccessScope.(current: T, next: T) -> Unit
) {
    val dataset = this[chartGroup]
    var index = start
    val dataSize = min(size, end)
    ChartDatasetAccessScopeInstance.internalChartGroup = chartGroup
    ChartDatasetAccessScopeInstance.groupIndex = chartGroups.indexOf(chartGroup)
    while (index + 1 < dataSize) {
        val currentItem = dataset[index]
        ChartDatasetAccessScopeInstance.internalCurrentItem = currentItem
        ChartDatasetAccessScopeInstance.index = index
        ChartDatasetAccessScopeInstance.action(currentItem, dataset[index + 1])
        index++
    }
}

inline fun <T> ChartDataset<T>.sumOf(
    start: Int = 0, end: Int = Int.MAX_VALUE, block: (T) -> Float
): Float {
    var sumValue = 0f
    forEachGroup { chartGroup ->
        forEach(chartGroup, start, end) { chartData ->
            sumValue += block(chartData)
        }
    }
    return sumValue
}

inline fun <T> ChartDataset<T>.maxOf(
    start: Int = 0, end: Int = Int.MAX_VALUE, block: (T) -> Float
): Float {
    var maxValue = Float.MIN_VALUE
    forEachGroup { chartGroup ->
        forEach(chartGroup, start, end) { chartData ->
            maxValue = maxValue.coerceAtLeast(block(chartData))
        }
    }
    return maxValue
}

inline fun <T> ChartDataset<T>.minOf(
    start: Int = 0, end: Int = Int.MAX_VALUE, block: (T) -> Float
): Float {
    var minValue = Float.MAX_VALUE
    chartGroups.forEach { group ->
        forEach(group, start, end) { chartData ->
            minValue = minValue.coerceAtMost(block(chartData))
        }
    }
    return minValue
}

inline fun <T> ChartDataset<T>.maxGroupValueOf(action: (T) -> Float): Float {
    var maxValue = Float.MIN_VALUE
    for (index in indices) {
        var sumValue = 0f
        forEachGroup { chartGroup ->
            val item = get(chartGroup)[index]
            sumValue += action(item)
        }
        maxValue = max(maxValue, sumValue)
    }
    return maxValue
}

inline fun <T> ChartDataset<T>.minGroupValueOf(action: (T) -> Float): Float {
    var minValue = Float.MAX_VALUE
    for (index in indices) {
        var sumValue = 0f
        forEachGroup { chartGroup ->
            val item = get(chartGroup)[index]
            sumValue += action(item)
        }
        minValue = min(minValue, sumValue)
    }
    return minValue
}

inline fun <T> ChartDataset<T>.computeGroupTotalValues(
    valueProvider: (T) -> Float
): List<Float> {
    val sumValueList = mutableListOf<Float>()
    for (index in indices) {
        var sumValue = 0f
        forEachGroup { groupName ->
            val chartData = get(groupName)[index]
            sumValue += valueProvider(chartData)
        }
        sumValueList.add(sumValue)
    }
    return sumValueList
}

interface ChartDataset<T> {
    val dataset: Map<String, List<T>>
    val size: Int
    val groupSize: Int
    val chartGroups: Set<String>
        get() = dataset.keys

    val indices: IntRange
        get() = 0 until size

    operator fun get(key: String): List<T> {
        return dataset[key] ?: emptyList()
    }

    fun transformDataset(action: (List<T>) -> List<T>)

    fun addChartData(chartGroup: String, chartData: List<T>)

    fun getChartGroupData(currentIndex: Int): List<T>
}

private open class MutableChartDataset<T> : ChartDataset<T> {

    private val internalMutableChartDataset = mutableMapOf<String, List<T>>()
    override val dataset: MutableMap<String, List<T>>
        get() = internalMutableChartDataset

    private var chartDatasetSize = 0
    override val size: Int
        get() = chartDatasetSize

    override val groupSize: Int
        get() = dataset.keys.size

    override fun getChartGroupData(currentIndex: Int): List<T> {
        val chartGroupData = ArrayList<T>()
        forEachGroup { chartGroup ->
            val item = dataset[chartGroup]?.get(currentIndex)
            if (null != item) {
                chartGroupData.add(item)
            }
        }
        return chartGroupData
    }

    override fun addChartData(chartGroup: String, chartData: List<T>) {
        if (0 == chartDatasetSize) {
            chartDatasetSize = chartData.size
        }
        assert(chartData.size == chartDatasetSize)
        internalMutableChartDataset[chartGroup] = chartData
    }

    override fun transformDataset(action: (List<T>) -> List<T>) {
        forEachGroup { groupName ->
            val dataset = dataset[groupName]
            if (null != dataset) {
                internalMutableChartDataset[groupName] = action(dataset)
            }
        }
    }

}