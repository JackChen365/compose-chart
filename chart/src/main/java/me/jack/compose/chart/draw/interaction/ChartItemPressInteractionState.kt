package me.jack.compose.chart.draw.interaction

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.Flow
import me.jack.compose.chart.context.ChartContext
import me.jack.compose.chart.context.ChartInteractionState
import me.jack.compose.chart.context.chartInteractionHandler
import me.jack.compose.chart.context.getInteractionState
import me.jack.compose.chart.interaction.ChartPressInteraction

val ChartContext.pressInteractionState: State<ChartPressInteraction>
    get() = chartInteractionHandler.getInteractionState<ChartItemPressInteractionState>().state

val ChartContext.pressInteractionLocation: Offset
    get() = chartInteractionHandler.getInteractionState<ChartItemPressInteractionState>().location

class ChartItemPressInteractionState : ChartInteractionState<ChartPressInteraction> {
    private val pressInteractionState = mutableStateOf<ChartPressInteraction>(ChartPressInteraction.Idle)
    private val pressInteractionLocation = mutableStateOf(Offset.Zero)
    override val state: State<ChartPressInteraction>
        get() = pressInteractionState
    override val location: Offset
        get() = pressInteractionLocation.value

    override suspend fun handleInteraction(interactions: Flow<Interaction>) {
        interactions.collect { interaction ->
            when (interaction) {
                is ChartPressInteraction.Press<*> -> {
                    if (pressInteractionLocation.value != interaction.pressLocation) {
                        pressInteractionState.value = interaction
                        pressInteractionLocation.value = interaction.pressLocation
                    }
                }

                is PressInteraction.Release -> {
                    val chartPressInteraction = pressInteractionState.value
                    if (chartPressInteraction is ChartPressInteraction.Press<*>) {
                        pressInteractionState.value =
                            ChartPressInteraction.Release(chartPressInteraction)
                    }
                }

                is PressInteraction.Cancel -> {
                    val chartPressInteraction = pressInteractionState.value
                    if (chartPressInteraction is ChartPressInteraction.Press<*>) {
                        pressInteractionState.value =
                            ChartPressInteraction.Cancel(chartPressInteraction)
                    }
                }
            }
        }
    }

}