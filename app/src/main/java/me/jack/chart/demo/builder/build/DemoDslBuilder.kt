package me.jack.chart.demo.builder.build

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import me.jack.chart.demo.builder.ActivityComposableDemo
import me.jack.chart.demo.builder.ComposableDemo
import me.jack.chart.demo.builder.Demo
import me.jack.chart.demo.builder.DemoCategory
import me.jack.chart.demo.builder.FragmentComposableDemo
import kotlin.reflect.KClass

class DemoDslBuilder : DemoBuilder<DemoDslBuilder.DemoScope.() -> Unit> {
    override fun buildDemoList(block: DemoScope.() -> Unit): List<Demo> {
        val demoScope = DemoScope()
        demoScope.apply(block)
        return demoScope.getCategoryList()
    }

    class DemoScope {
        private val categoryInternalList: MutableList<Demo> = mutableListOf()

        fun getCategoryList(): List<Demo> = categoryInternalList

        fun demo(title: String, block: @Composable () -> Unit) {
            val demoItem = ComposableDemo(title, block)
            categoryInternalList.add(demoItem)
        }

        fun fragmentDemo(title: String, fragmentClass: KClass<Fragment>) {
            val demoItem = FragmentComposableDemo(title, fragmentClass)
            categoryInternalList.add(demoItem)
        }

        fun<T : Activity> activityDemo(title: String, activityClass: KClass<T>) {
            val demoItem = ActivityComposableDemo(title, activityClass)
            categoryInternalList.add(demoItem)
        }

        fun category(title: String, block: DemoCategory.() -> Unit) {
            val categoryItem = DemoCategory(title)
            categoryItem.apply(block)
            categoryInternalList.add(categoryItem)
        }
    }
}