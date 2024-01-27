package me.jack.chart.demo.builder.build

import me.jack.chart.demo.builder.Demo

interface DemoBuilder<T> {
    fun buildDemoList(demoList: T): List<Demo>
}