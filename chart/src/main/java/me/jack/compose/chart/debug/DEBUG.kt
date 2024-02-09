package me.jack.compose.chart.debug

private const val DEBUG = true
internal inline fun debugLog(generateMsg: () -> String) {
    if (DEBUG) {
        println("compose-chart: ${generateMsg()}")
    }
}