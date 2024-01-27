package me.jack.compose.chart.draw.interaction

import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.Flow
import me.jack.compose.chart.context.ChartContext
import me.jack.compose.chart.context.ChartInteractionState
import me.jack.compose.chart.context.chartInteractionHandler
import me.jack.compose.chart.context.getInteractionState

val ChartContext.hoverState: State<Boolean>
    get() = chartInteractionHandler.getInteractionState<ChartHoverInteractionState>().state

class ChartHoverInteractionState : ChartInteractionState<Boolean> {
    private val hoverState = mutableStateOf(false)
    private val hoverLocation = mutableStateOf(Offset.Zero)
    override val state: State<Boolean>
        get() = hoverState
    override val location: Offset
        get() = hoverLocation.value

    override suspend fun handleInteraction(interactions: Flow<Interaction>) {
        interactions.collect { interaction ->
            when (interaction) {
                is HoverInteraction.Enter -> {
                    hoverState.value = true
                }

                is HoverInteraction.Exit -> {
                    hoverState.value = false
                }
            }
        }
    }

}