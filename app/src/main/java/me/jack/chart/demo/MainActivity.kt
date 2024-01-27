package me.jack.chart.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import me.jack.chart.demo.builder.buildComposableDemoList
import me.jack.chart.demo.builder.initialAndDisplayComposeClassDemoList
import me.jack.chart.demo.ui.BarDemos
import me.jack.chart.demo.ui.BubbleDemos
import me.jack.chart.demo.ui.CandleStickDemos
import me.jack.chart.demo.ui.CombinedChartDemos
import me.jack.chart.demo.ui.DonutDemos
import me.jack.chart.demo.ui.LineDemos
import me.jack.chart.demo.ui.PieDemos

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialAndDisplayComposeClassDemoList(
            getString(R.string.app_name),
            BarDemos::class.java,
            LineDemos::class.java,
            BubbleDemos::class.java,
            DonutDemos::class.java,
            PieDemos::class.java,
            CandleStickDemos::class.java,
            CombinedChartDemos::class.java
        )
    }
}