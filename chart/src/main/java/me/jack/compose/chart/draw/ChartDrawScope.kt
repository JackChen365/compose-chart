package me.jack.compose.chart.draw

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.CoroutineScope
import me.jack.compose.chart.animation.ChartAnimatableState
import me.jack.compose.chart.animation.ChartColorAnimatableState
import me.jack.compose.chart.animation.ChartFloatAnimatableState
import me.jack.compose.chart.animation.ChartIntAnimatableState
import me.jack.compose.chart.animation.ChartOffsetAnimatableState
import me.jack.compose.chart.animation.ChartSizeAnimatableState
import me.jack.compose.chart.animation.colorAnimatableState
import me.jack.compose.chart.animation.floatAnimatableState
import me.jack.compose.chart.animation.intAnimatableState
import me.jack.compose.chart.animation.offsetAnimatableState
import me.jack.compose.chart.animation.sizeAnimatableState
import me.jack.compose.chart.component.TapGestures
import me.jack.compose.chart.context.ChartContext
import me.jack.compose.chart.context.chartInteractionHandler
import me.jack.compose.chart.context.pressLocation
import me.jack.compose.chart.context.pressState
import me.jack.compose.chart.context.tryEmit
import me.jack.compose.chart.draw.DrawElement.Companion.getCachedDrawElement
import me.jack.compose.chart.draw.interaction.doubleTapLocation
import me.jack.compose.chart.draw.interaction.longPressLocation
import me.jack.compose.chart.draw.interaction.longPressTapState
import me.jack.compose.chart.draw.interaction.pressInteractionState
import me.jack.compose.chart.draw.interaction.tapLocation
import me.jack.compose.chart.draw.interaction.tapState
import me.jack.compose.chart.interaction.ChartPressInteraction
import me.jack.compose.chart.interaction.asPressInteraction
import me.jack.compose.chart.model.ChartDataset
import me.jack.compose.chart.scope.ChartDatasetAccessScope
import me.jack.compose.chart.scope.ChartDatasetAccessScopeInstance
import me.jack.compose.chart.scope.SingleChartScope
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

@Composable
fun <T> SingleChartScope<T>.ChartCanvas(
    modifier: Modifier = Modifier,
    onDraw: ChartDrawScope<T>.() -> Unit
) {
    var chartDrawScope: ChartDrawScope<T>? = remember { null }
    val scope = rememberCoroutineScope()
    Spacer(
        modifier = modifier
            .drawBehind {
                if (null == chartDrawScope) {
                    chartDrawScope = ChartDrawScope(
                        singleChartScope = this@ChartCanvas,
                        drawScope = this,
                        scope = scope,
                        tapGestures = tapGestures
                    )
                }
                onDraw.invoke(checkNotNull(chartDrawScope).also { it.reset() })
            }
    )
}

class ChartDrawScope<T>(
    val singleChartScope: SingleChartScope<T>,
    private val drawScope: DrawScope,
    private val scope: CoroutineScope,
    private val tapGestures: TapGestures<T>
) : DrawScope by drawScope, ChartDatasetAccessScope by ChartDatasetAccessScopeInstance {
    val chartContext: ChartContext = singleChartScope.chartContext
    val chartDataset: ChartDataset<T> = singleChartScope.chartDataset
    private val traceableDrawScope = TraceableDrawScope(this).also { drawScope ->
        drawScope.onDrawElementUpdated { drawElement, index, currentItem ->
            currentDrawElement = drawElement
            trackDrawElementInteraction(drawElement, currentItem, index)
        }
    }
    private var currentDrawElement: DrawElement = DrawElement.None
    private val animationStateCaching = mutableMapOf<Class<*>, MutableList<ChartAnimatableState<*, *>>>()
    private var animationChildIds = mutableMapOf<Class<*>, Int>()

    fun reset() {
        animationChildIds.clear()
    }

    fun clickableRect(
        topLeft: Offset,
        size: Size,
        focusPoint: Offset = Offset.Unspecified
    ) {
        val drawElement: DrawElement.Rect = getCachedDrawElement()
        drawElement.topLeft = topLeft
        drawElement.size = size
        drawElement.focusPoint = focusPoint
        currentDrawElement = drawElement
        trackDrawElementInteraction(drawElement, currentItem(), index)
    }

    fun clickable(
        currentItem: T,
        index: Int,
        block: TraceableDrawScope<T>.() -> Unit
    ) {
        traceableDrawScope.trackChartData(currentItem, index)
        // invoke twice, first we update the draw element, and than the second time we draw the element.
        block.invoke(traceableDrawScope)
        block.invoke(traceableDrawScope)
    }

    fun ChartDatasetAccessScope.clickable(
        block: TraceableDrawScope<T>.() -> Unit
    ) {
        traceableDrawScope.trackChartData(currentItem(), index)
        // invoke twice, first we update the draw element, and than the second time we draw the element.
        block.invoke(traceableDrawScope)
        block.invoke(traceableDrawScope)
    }

    val currentLeftTopOffset: Offset
        get() {
            return with(singleChartScope) {
                with(singleChartScope.contentMeasurePolicy) {
                    childLeftTop(groupCount, groupIndex, index)
                }
            }
        }

    val nextLeftTopOffset: Offset
        get() {
            return with(singleChartScope) {
                with(singleChartScope.contentMeasurePolicy) {
                    childLeftTop(groupCount, groupIndex, index + 1)
                }
            }
        }

    val childCenterOffset: Offset
        get() {
            return with(singleChartScope.contentMeasurePolicy) {
                Offset(
                    x = currentLeftTopOffset.x + childSize.width / 2,
                    y = currentLeftTopOffset.y + childSize.height / 2
                )
            }
        }

    val nextChildCenterOffset: Offset
        get() {
            return with(singleChartScope.contentMeasurePolicy) {
                Offset(
                    x = nextLeftTopOffset.x + childSize.width / 2,
                    y = nextLeftTopOffset.y + childSize.height / 2
                )
            }
        }

    val childSize: Size
        get() = singleChartScope.contentMeasurePolicy.childSize

    val childOffsets: Offset
        get() = with(singleChartScope.contentMeasurePolicy) {
            Offset(
                x = childSize.width + childDividerSize,
                y = childSize.height + childDividerSize
            )
        }

    infix fun Color.whenPressed(targetValue: Color): Color {
        return valueIf(targetValue = targetValue, condition = ::isPressed)
    }

    private fun Color.valueIf(
        targetValue: Color, condition: () -> Boolean
    ): Color {
        return if (condition()) targetValue else this
    }

    infix fun Int.whenPressedAnimateTo(targetValue: Int): Int {
        return animateToIf(targetValue = targetValue, condition = ::isPressed)
    }

    infix fun Float.whenPressedAnimateTo(targetValue: Float): Float {
        return animateToIf(targetValue = targetValue, condition = ::isPressed)
    }

    infix fun Color.whenPressedAnimateTo(targetValue: Color): Color {
        return animateToIf(targetValue = targetValue, condition = ::isPressed)
    }

    infix fun Offset.whenPressedAnimateTo(targetValue: Offset): Offset {
        return animateToIf(targetValue = targetValue, condition = ::isPressed)
    }

    infix fun Size.whenPressedAnimateTo(targetValue: Size): Size {
        return animateToIf(targetValue = targetValue, condition = ::isPressed)
    }

    private fun isPressed(): Boolean {
        return chartContext.pressState.value && chartContext.pressLocation in currentDrawElement
    }

    fun Int.animateToIf(
        targetValue: Int, condition: () -> Boolean
    ): Int {
        val intAnimatableState = intAnimationState(this)
        intAnimatableState.value = if (condition()) targetValue else this
        return intAnimatableState.value
    }

    fun Float.animateToIf(
        targetValue: Float, condition: () -> Boolean
    ): Float {
        val floatAnimatableState = floatAnimationState(this)
        floatAnimatableState.value = if (condition()) targetValue else this
        return floatAnimatableState.value
    }

    fun Color.animateToIf(
        targetValue: Color, condition: () -> Boolean
    ): Color {
        val colorAnimatableState = colorAnimationState(this)
        colorAnimatableState.value = if (condition()) targetValue else this
        return colorAnimatableState.value
    }

    fun Offset.animateToIf(
        targetValue: Offset = Offset.Zero, condition: () -> Boolean
    ): Offset {
        val offsetAnimatableState = offsetAnimationState(this)
        offsetAnimatableState.value = if (condition()) targetValue else this
        return offsetAnimatableState.value
    }

    fun Size.animateToIf(targetValue: Size = Size.Zero, condition: () -> Boolean): Size {
        val sizeAnimatableState = sizeAnimationState(this)
        sizeAnimatableState.value = if (condition()) targetValue else this
        return sizeAnimatableState.value
    }

    private fun trackDrawElementInteraction(
        drawElement: DrawElement,
        currentItem: T,
        currentIndex: Int
    ) {
        if (chartContext.pressState.value && chartContext.pressLocation in drawElement) {
            val pressInteractionState = chartContext.pressInteractionState.value.asPressInteraction<T>()
            if (null == pressInteractionState) {
                val chartGroupData = chartDataset.getChartGroupData(currentIndex)
                chartContext.chartInteractionHandler.tryEmit(
                    ChartPressInteraction.Press(
                        pressLocation = chartContext.pressLocation,
                        drawElement = drawElement.clone(),
                        currentItem = currentItem,
                        currentGroupItems = chartGroupData
                    )
                )
            }
        }
        if (chartContext.tapState.value && chartContext.tapLocation in drawElement) {
            tapGestures.onTap.invoke(currentItem)
        }
        if (chartContext.pressState.value && chartContext.doubleTapLocation in drawElement) {
            tapGestures.onDoubleTap.invoke(currentItem)
        }
        if (chartContext.longPressTapState.value && chartContext.longPressLocation in drawElement) {
            tapGestures.onLongPress.invoke(currentItem)
        }
    }

    private inline fun <reified T : ChartAnimatableState<*, *>> getCachedAnimatableState(): T? {
        val key = T::class.java
        val animatableStates = animationStateCaching.getOrPut(key) { mutableListOf() }
        val animationChildId = animationChildIds.getOrDefault(key = key, 0)
        animationChildIds[key] = animationChildId + 1
        return animatableStates.getOrNull(animationChildId + 1) as? T
    }

    private inline fun <reified T : ChartAnimatableState<*, *>> addCachedAnimatableState(animatableState: T) {
        val key = animatableState::class.java
        val animatableStates = animationStateCaching.getOrPut(key) { mutableListOf() }
        animatableStates.add(animatableState)
    }

    fun intAnimationState(initialValue: Int = 0): ChartIntAnimatableState {
        return getCachedAnimatableState() ?: intAnimatableState(initialValue = initialValue, scope = scope).also {
            addCachedAnimatableState(it)
        }
    }

    fun floatAnimationState(
        initialValue: Float = 0f
    ): ChartFloatAnimatableState {
        return getCachedAnimatableState<ChartFloatAnimatableState>()?.also { state ->
            state.reset(initialValue)
        } ?: floatAnimatableState(
            scope = scope, initialValue = initialValue
        ).also {
            addCachedAnimatableState(it)
        }
    }

    fun colorAnimationState(initialValue: Color = Color.Transparent): ChartColorAnimatableState {
        return getCachedAnimatableState<ChartColorAnimatableState>()?.also { state ->
            state.reset(initialValue)
        } ?: colorAnimatableState(initialValue = initialValue, scope = scope).also {
            addCachedAnimatableState(it)
        }
    }

    fun sizeAnimationState(initialValue: Size = Size.Zero): ChartSizeAnimatableState {
        return getCachedAnimatableState<ChartSizeAnimatableState>()?.also { state ->
            state.reset(initialValue)
        } ?: sizeAnimatableState(initialValue = initialValue, scope = scope).also {
            addCachedAnimatableState(it)
        }
    }

    fun offsetAnimationState(initialValue: Offset = Offset.Zero): ChartOffsetAnimatableState {
        return getCachedAnimatableState<ChartOffsetAnimatableState>()?.also { state ->
            state.reset(initialValue)
        } ?: offsetAnimatableState(initialValue = initialValue, scope = scope).also {
            addCachedAnimatableState(it)
        }
    }
}

class TraceableDrawScope<T>(
    private val drawScope: ChartDrawScope<T>
) : DrawScope by drawScope {
    private var onDrawElementUpdated: ((drawElement: DrawElement, index: Int, currentItem: T) -> Unit)? = null
    private var isCurrentDrawElementUpdated = false
    private var currentItem: T? = null
    private var currentIndex: Int = 0

    val currentLeftTopOffset: Offset
        get() = with(drawScope) { currentLeftTopOffset }

    val nextLeftTopOffset: Offset
        get() = with(drawScope) { nextLeftTopOffset }

    val childCenterOffset: Offset
        get() = with(drawScope) { childCenterOffset }

    val childSize: Size
        get() = with(drawScope) { childSize }

    infix fun Color.whenPressed(targetValue: Color): Color {
        return with(drawScope) { whenPressed(targetValue) }
    }

    infix fun Int.whenPressedAnimateTo(targetValue: Int): Int {
        return with(drawScope) { whenPressedAnimateTo(targetValue) }
    }

    infix fun Float.whenPressedAnimateTo(targetValue: Float): Float {
        return with(drawScope) { whenPressedAnimateTo(targetValue) }
    }

    infix fun Color.whenPressedAnimateTo(targetValue: Color): Color {
        return with(drawScope) { whenPressedAnimateTo(targetValue) }
    }

    infix fun Offset.whenPressedAnimateTo(targetValue: Offset): Offset {
        return with(drawScope) { whenPressedAnimateTo(targetValue) }
    }

    infix fun Size.whenPressedAnimateTo(targetValue: Size): Size {
        return with(drawScope) { whenPressedAnimateTo(targetValue) }
    }

    override fun drawRect(
        brush: Brush,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        drawElement(
            onUpdateDrawElement = {
                val rectDrawElement: DrawElement.Rect = getCachedDrawElement()
                rectDrawElement.topLeft = topLeft
                rectDrawElement.size = size
                onDrawElementUpdated?.invoke(rectDrawElement, currentIndex, checkNotNull(currentItem))
            },
            onDraw = {
                drawScope.drawRect(brush, topLeft, size, alpha, style, colorFilter, blendMode)
            }
        )
    }

    override fun drawRect(
        color: Color,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        drawElement(
            onUpdateDrawElement = {
                val rectDrawElement: DrawElement.Rect = getCachedDrawElement()
                rectDrawElement.color = color
                rectDrawElement.topLeft = topLeft
                rectDrawElement.size = size
                onDrawElementUpdated?.invoke(rectDrawElement, currentIndex, checkNotNull(currentItem))
            },
            onDraw = {
                drawScope.drawRect(color, topLeft, size, alpha, style, colorFilter, blendMode)
            }
        )
    }

    override fun drawCircle(
        brush: Brush,
        radius: Float,
        center: Offset,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        drawElement(
            onUpdateDrawElement = {
                val circleDrawElement: DrawElement.Circle = getCachedDrawElement()
                circleDrawElement.radius = radius
                circleDrawElement.center = center
                onDrawElementUpdated?.invoke(circleDrawElement, currentIndex, checkNotNull(currentItem))
            },
            onDraw = {
                drawScope.drawCircle(brush, radius, center, alpha, style, colorFilter, blendMode)
            }
        )
    }

    override fun drawCircle(
        color: Color,
        radius: Float,
        center: Offset,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        drawElement(
            onUpdateDrawElement = {
                val circleDrawElement: DrawElement.Circle = getCachedDrawElement()
                circleDrawElement.color = color
                circleDrawElement.radius = radius
                circleDrawElement.center = center
                onDrawElementUpdated?.invoke(circleDrawElement, currentIndex, checkNotNull(currentItem))
            },
            onDraw = {
                drawScope.drawCircle(color, radius, center, alpha, style, colorFilter, blendMode)
            }
        )
    }

    override fun drawOval(
        brush: Brush,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        drawElement(
            onUpdateDrawElement = {
                val ovalDrawElement: DrawElement.Oval = getCachedDrawElement()
                ovalDrawElement.topLeft = topLeft
                ovalDrawElement.size = size
                onDrawElementUpdated?.invoke(ovalDrawElement, currentIndex, checkNotNull(currentItem))
            },
            onDraw = {
                drawScope.drawOval(brush, topLeft, size, alpha, style, colorFilter, blendMode)
            }
        )
    }

    override fun drawOval(
        color: Color,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        drawElement(
            onUpdateDrawElement = {
                val ovalDrawElement: DrawElement.Oval = getCachedDrawElement()
                ovalDrawElement.color = color
                ovalDrawElement.topLeft = topLeft
                ovalDrawElement.size = size
                onDrawElementUpdated?.invoke(ovalDrawElement, currentIndex, checkNotNull(currentItem))
            },
            onDraw = {
                drawScope.drawOval(color, topLeft, size, alpha, style, colorFilter, blendMode)
            }
        )
    }

    override fun drawArc(
        color: Color,
        startAngle: Float,
        sweepAngle: Float,
        useCenter: Boolean,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        drawElement(
            onUpdateDrawElement = {
                val arcDrawElement: DrawElement.Arc = getCachedDrawElement()
                arcDrawElement.color = color
                arcDrawElement.startAngle = startAngle
                arcDrawElement.startAngle = startAngle
                arcDrawElement.leftTop = topLeft
                arcDrawElement.size = size
                arcDrawElement.sweepAngle = sweepAngle
                if (style is Stroke) {
                    arcDrawElement.strokeWidth = style.width
                }
                onDrawElementUpdated?.invoke(arcDrawElement, currentIndex, checkNotNull(currentItem))
            },
            onDraw = {
                drawScope.drawArc(
                    color,
                    startAngle,
                    sweepAngle,
                    useCenter,
                    topLeft,
                    size,
                    alpha,
                    style,
                    colorFilter,
                    blendMode
                )
            }
        )
    }

    override fun drawArc(
        brush: Brush,
        startAngle: Float,
        sweepAngle: Float,
        useCenter: Boolean,
        topLeft: Offset,
        size: Size,
        alpha: Float,
        style: DrawStyle,
        colorFilter: ColorFilter?,
        blendMode: BlendMode
    ) {
        drawElement(
            onUpdateDrawElement = {
                val arcDrawElement: DrawElement.Arc = getCachedDrawElement()
                arcDrawElement.startAngle = startAngle
                arcDrawElement.leftTop = topLeft
                arcDrawElement.size = size
                arcDrawElement.sweepAngle = sweepAngle
                if (style is Stroke) {
                    arcDrawElement.strokeWidth = style.width
                }
                onDrawElementUpdated?.invoke(arcDrawElement, currentIndex, checkNotNull(currentItem))
            },
            onDraw = {
                drawScope.drawArc(
                    brush,
                    startAngle,
                    sweepAngle,
                    useCenter,
                    topLeft,
                    size,
                    alpha,
                    style,
                    colorFilter,
                    blendMode
                )
            }
        )
    }

    private inline fun drawElement(
        onUpdateDrawElement: () -> Unit,
        onDraw: () -> Unit
    ) {
        try {
            if (!isCurrentDrawElementUpdated) {
                onUpdateDrawElement()
            } else {
                onDraw()
            }
        } finally {
            isCurrentDrawElementUpdated = !isCurrentDrawElementUpdated
        }
    }

    fun onDrawElementUpdated(
        onDrawElementUpdated: (drawElement: DrawElement, index: Int, T) -> Unit
    ) {
        this.onDrawElementUpdated = onDrawElementUpdated
    }

    fun trackChartData(currentItem: T, index: Int) {
        this.currentItem = currentItem
        this.currentIndex = index
    }
}

sealed class DrawElement : Cloneable {
    companion object {
        internal val drawElementCaching = mutableMapOf<Class<*>, DrawElement>()
        internal inline fun <reified T : DrawElement> getCachedDrawElement(): T {
            val key = T::class.java
            return drawElementCaching.getOrPut(key = key) {
                when (key) {
                    Rect::class.java -> Rect()
                    Circle::class.java -> Circle()
                    Oval::class.java -> Oval()
                    Arc::class.java -> Arc()
                    else -> None
                }
            } as T
        }
    }

    open operator fun contains(location: Offset): Boolean = false

    public override fun clone(): DrawElement {
        return super.clone() as DrawElement
    }

    data object None : DrawElement()

    class Rect : DrawElement() {
        var color: Color = Color.Unspecified
        var topLeft: Offset = Offset.Zero
        var size: Size = Size.Zero
        var focusPoint: Offset = Offset.Unspecified
        override operator fun contains(location: Offset): Boolean {
            return location.intersect(topLeft, size)
        }

        override fun clone(): Rect {
            val rect = super.clone() as Rect
            rect.topLeft = topLeft
            rect.size = size
            return rect
        }

        override fun toString(): String {
            return "Rect(topLeft=$topLeft, size=$size)"
        }
    }

    class Circle : DrawElement() {
        var color: Color = Color.Unspecified
        var radius: Float = 0f
        var center: Offset = Offset.Zero

        override operator fun contains(location: Offset): Boolean {
            return location.intersectCircle(center, radius)
        }

        override fun clone(): Circle {
            val rect = super.clone() as Circle
            rect.radius = radius
            rect.center = center
            return rect
        }

        override fun toString(): String {
            return "Circle(radius=$radius, center=$center)"
        }
    }

    @Stable
    class Oval : DrawElement() {
        var color: Color = Color.Unspecified
        var topLeft: Offset = Offset.Zero
        var size: Size = Size.Zero
        override operator fun contains(location: Offset): Boolean {
            return location.intersectOval(topLeft + size.center, size)
        }

        override fun clone(): Oval {
            val rect = super.clone() as Oval
            rect.topLeft = topLeft
            rect.size = size
            return rect
        }

        override fun toString(): String {
            return "Oval(topLeft=$topLeft, size=$size)"
        }
    }

    @Stable
    class Arc : DrawElement() {
        var color: Color = Color.Unspecified
        var leftTop: Offset = Offset.Zero
        var size: Size = Size.Zero
        var startAngle: Float = 0f
        var sweepAngle: Float = 0f
        var strokeWidth: Float = 0f
        override operator fun contains(location: Offset): Boolean {
            return if (0 < strokeWidth) {
                location.intersectArcStrokeWidth(
                    leftTop = leftTop,
                    size = size,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    strokeWidth = strokeWidth
                )
            } else {
                location.intersectArc(
                    leftTop = leftTop,
                    size = size,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle
                )
            }
        }

        override fun clone(): Arc {
            val arc = super.clone() as Arc
            arc.leftTop = leftTop
            arc.size = size
            arc.startAngle = startAngle
            arc.sweepAngle = sweepAngle
            arc.strokeWidth = strokeWidth
            return arc
        }

        override fun toString(): String {
            return "Arc(color=$color, leftTop=$leftTop, size=$size, startAngle=$startAngle, sweepAngle=$sweepAngle, strokeWidth=$strokeWidth)"
        }
    }
}

fun Offset.intersect(topLeft: Offset, size: Size): Boolean {
    return x in topLeft.x..topLeft.x + size.width && y in topLeft.y..topLeft.y + size.height
}

fun Offset.intersectCircle(center: Offset, radius: Float): Boolean {
    val distanceSquared = (x - center.x) * (x - center.x) + (y - center.y) * (y - center.y)
    return distanceSquared <= radius * radius
}

fun Offset.intersectOval(center: Offset, size: Size): Boolean {
    val term1 = ((x - center.x) * (x - center.x)) / (size.width * size.width)
    val term2 = ((y - center.y) * (y - center.y)) / (size.height * size.height)
    return (term1 + term2) < 1
}

fun Offset.intersectArc(
    leftTop: Offset, size: Size, startAngle: Float, sweepAngle: Float
): Boolean {
    val centerX = leftTop.x + size.width / 2
    val centerY = leftTop.y + size.height / 2

    val dx = x - centerX
    val dy = y - centerY

    val distance = sqrt(dx * dx + dy * dy)
    var angle = atan2(dy, dx) * (180 / PI)
    if (angle < 0) angle += 360.0

    val start = startAngle % 360
    val end = (start + sweepAngle) % 360

    val isWithinDistance = distance <= size.width / 2
    val isWithinAngles = if (start < end) {
        angle in start..end
    } else {
        angle in 0f..end || angle in start..360f
    }
    return isWithinDistance && isWithinAngles
}

fun Offset.intersectArcStrokeWidth(
    leftTop: Offset, size: Size, startAngle: Float, sweepAngle: Float, strokeWidth: Float
): Boolean {
    val centerX = leftTop.x + size.width / 2
    val centerY = leftTop.y + size.height / 2

    val dx = x - centerX
    val dy = y - centerY

    val distance = sqrt(dx * dx + dy * dy)
    var angle = atan2(dy, dx) * (180 / PI)
    if (angle < 0) angle += 360.0

    val start = startAngle % 360
    val end = (start + sweepAngle) % 360

    val isWithinDistance =
        distance < (size.width / 2 + strokeWidth/ 2) && distance > (size.width / 2 - strokeWidth)
    val isWithinAngles = if (start < end) {
        angle in start..end
    } else {
        angle in 0f..end || angle in start..360f
    }
    return isWithinDistance && isWithinAngles
}
