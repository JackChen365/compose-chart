package me.jack.compose.chart.interaction

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.ui.geometry.Offset

interface ChartTapInteraction : Interaction {

    class Tap(val location: Offset) : ChartTapInteraction

    class DoubleTap(val location: Offset) : ChartTapInteraction

    class LongPress(val location: Offset) : ChartTapInteraction
}