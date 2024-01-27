package me.jack.chart.demo.builder.build

import androidx.compose.runtime.Composer
import androidx.compose.runtime.currentComposer
import me.jack.chart.demo.builder.utils.CommonPreviewUtils.invokeComposableMethod
import me.jack.chart.demo.builder.Demo
import me.jack.chart.demo.builder.DemoCategory
import java.lang.reflect.Modifier

class ComposableDemoBuilder : DemoBuilder<List<Class<*>>> {
    override fun buildDemoList(demoList: List<Class<*>>): List<Demo> {
        val categoryList = mutableListOf<DemoCategory>()
        demoList.forEach { clazz ->
            categoryList.add(resolveClass(clazz))
        }
        return categoryList
    }

    private fun resolveClass(clazz: Class<*>): DemoCategory {
        //The composable annotation only visible in binary code. So here we check the first parameter...
        val methodList = clazz.methods.filter { method ->
            method.parameterTypes.firstOrNull() == Composer::class.java
        }
        val categoryItem = DemoCategory(clazz.simpleName)
        methodList.forEach { method ->
            categoryItem.demo(method.name) {
                if (Modifier.isStatic(method.modifiers)) {
                    // This is a top level or static method
                    method.invokeComposableMethod(null, currentComposer)
                } else {
                    // The method is part of a class. We try to instantiate the class with an empty
                    // constructor.
                    val instance = clazz.getConstructor().newInstance()
                    method.invokeComposableMethod(instance, currentComposer)
                }
            }
        }
        return categoryItem
    }
}