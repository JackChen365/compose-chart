package me.jack.compose.chart.interaction

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.ui.geometry.Offset
import me.jack.compose.chart.draw.DrawElement

fun <T> ChartPressInteraction.asPressInteraction(): ChartPressInteraction.Press<T>? {
    @Suppress("UNCHECKED_CAST")
    return this as? ChartPressInteraction.Press<T>
}

interface ChartPressInteraction : Interaction {
    object Idle : ChartPressInteraction

    class Press<T>(
        val pressLocation: Offset,
        val drawElement: DrawElement,
        val currentItem: T,
        val currentGroupItems: List<T>
    ) : ChartPressInteraction

    class Release<T>(val press: Press<T>) : ChartPressInteraction

    class Cancel<T>(val press: Press<T>) : ChartPressInteraction
}
