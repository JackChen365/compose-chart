package me.jack.chart.demo.builder

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import me.jack.chart.demo.builder.build.ComposableDemoBuilder
import me.jack.chart.demo.builder.build.DemoDslBuilder
import java.util.ArrayDeque

/**
 * Build the demo list by DSL
 */
fun buildAppDemo(block: DemoDslBuilder.DemoScope.() -> Unit): List<Demo> {
    val builder = DemoDslBuilder()
    return builder.buildDemoList(block)
}

/**
 * Build the demo list by composable annotation
 */
fun buildComposableDemoList(vararg classArray: Class<*>): List<Demo> {
    val builder = ComposableDemoBuilder()
    return builder.buildDemoList(classArray.toList())
}

fun ComponentActivity.initialAndDisplayDemoList(appName: String, block: DemoDslBuilder.DemoScope.() -> Unit) {
    val demoList = buildAppDemo(block)
    val rootCategory = DemoCategory(appName, demoList.toMutableList())
    buildActivityDemoList(rootCategory)
}

fun ComponentActivity.initialAndDisplayComposeClassDemoList(
    appName: String,
    vararg classArray: Class<*>
) {
    val demoList = buildComposableDemoList(*classArray)
    val rootCategory = DemoCategory(appName, demoList.toMutableList())
    buildActivityDemoList(rootCategory)
}

private fun ComponentActivity.buildActivityDemoList(rootCategory: DemoCategory) {
    val demoNavigator = DemoNavigator(
        backDispatcher = onBackPressedDispatcher,
        rootDemo = rootCategory
    ) { navigator, demo ->
        //Going backward.
        navigator.backTo(demo)
    }
    setContent {
        val context = LocalContext.current
        AppDemo(demoNavigator, demoNavigator.currentDemo) { navigator, demo ->
            if (demo is ActivityComposableDemo<*>) {
                context.startActivity(Intent(context, demo.activityClass.java).also {
                    it.putExtra(
                        "title",
                        demo.title
                    )
                })
            } else {
                // Going forward
                navigator.navigateTo(demo)
            }
        }
    }
}

class DemoNavigator(
    private val rootDemo: Demo,
    private val backDispatcher: OnBackPressedDispatcher,
    private val backStack: ArrayDeque<Demo> = ArrayDeque(),
    private val onBackPressed: (DemoNavigator, Demo) -> Unit,
) {
    private var _currentDemo by mutableStateOf(rootDemo)
    var currentDemo: Demo
        get() = _currentDemo
        private set(value) {
            _currentDemo = value
            onBackPressedCallback.isEnabled = !isRoot()
        }

    init {
        backStack.push(rootDemo)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            popBackStack()
        }
    }.apply {
        isEnabled = !isRoot()
        backDispatcher.addCallback(this)
    }

    fun isRoot() = backStack.peek() == rootDemo

    fun navigateTo(demo: Demo) {
        currentDemo = demo
        backStack.push(demo)
        onBackPressedCallback.isEnabled = !isRoot()
    }

    fun backTo(demo: Demo) {
        currentDemo = demo
        onBackPressedCallback.isEnabled = !isRoot()
    }

    fun popBackStack() {
        if (!backStack.isEmpty()) {
            backStack.pop()
            val demo = backStack.peek()
            onBackPressed(this, demo)
        }
        onBackPressedCallback.isEnabled = !isRoot()
    }

}