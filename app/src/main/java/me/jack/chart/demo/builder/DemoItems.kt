package me.jack.chart.demo.builder

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

/**
 * Generic demo with a [title] that will be displayed in the list of demos.
 */
sealed class Demo(val title: String) {
    override fun toString() = title
}

class ComposableDemo(title: String, var demo: (@Composable () -> Unit)) : Demo(title)

class FragmentComposableDemo<T : Fragment>(title: String, val fragmentClass: KClass<T>) : Demo(title)

class ActivityComposableDemo<T : Activity>(title: String, val activityClass: KClass<T>) : Demo(title)

class DemoCategory(
    title: String,
    private val demoListInternal: MutableList<Demo> = mutableListOf()
) : Demo(title) {

    val demoList: List<Demo> = demoListInternal

    fun category(title: String, block: DemoCategory.() -> Unit) {
        val categoryItem = DemoCategory(title)
        categoryItem.apply(block)
        demoListInternal.add(categoryItem)
    }

    fun<T : Fragment> fragmentDemo(title: String, fragmentClass: KClass<T>) {
        val demoItem = FragmentComposableDemo(title, fragmentClass)
        demoListInternal.add(demoItem)
    }

    fun<T : Activity> activityDemo(title: String, activityClass: KClass<T>) {
        val demoItem = ActivityComposableDemo(title, activityClass)
        demoListInternal.add(demoItem)
    }

    fun demo(title: String, block: @Composable () -> Unit) {
        val demoItem = ComposableDemo(title, block)
        demoListInternal.add(demoItem)
    }
}
