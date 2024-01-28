## Readme

A single but powerful compose chart library.

- [How to use](#how-to-use)
    - [Compile & Sample](#Compile)
    - [Preview](#Preview)
    - [Line Chart](#case1-using-default-dsl-to-generate-your-test-project)
    - [Bar Chart](#case2-mix-the-dsl-with-an-existed-project)
- [FAQ](#faq)
- [Links](#links)

### Compile

  <summary>Kotlin</summary>
  
  ```kotlin
  dependencies {
      todo()
  }
  ```



[Sample APK](https://github.com/JackChen365/compose-chart/raw/master/sample/app-debug.apk)

### Preview

* LineChart

  https://github.com/JackChen365/compose-chart/assets/12761044/4418e27e-711c-4d23-9d57-644303304522
  
  https://github.com/JackChen365/compose-chart/assets/12761044/8498c098-07f8-4403-9e3b-7b8dfddf7c57

* BarChart

  https://github.com/JackChen365/compose-chart/assets/12761044/1361aaa3-9f85-4a86-9466-6f4ec16863a5
  
  https://github.com/JackChen365/compose-chart/assets/12761044/6a54b6b1-b5c6-4f3e-9256-60d8b27db9ec

* Bubble chart

  https://github.com/JackChen365/compose-chart/assets/12761044/d13ea274-8910-4644-bbe0-1e2be29469e9

* CandelChart

  https://github.com/JackChen365/compose-chart/assets/12761044/17810c23-147f-4b7e-89f7-f7bdd6f21411

* PieChart & DonutChart

  https://github.com/JackChen365/compose-chart/assets/12761044/0f9d47d8-ca77-4b7a-88f9-242966675905
  
  https://github.com/JackChen365/compose-chart/assets/12761044/e4adb81d-136a-43f6-82ad-5a3e9962f331

* Combined Chart

  https://github.com/JackChen365/compose-chart/assets/12761044/7fbdaf18-5e81-4901-bb87-17441051f871


### Line Chart

  ```
  // 1. build dataset
  val dataset = chartDataGroup<LineData> {
      repeat(3) {
          val groupColor = Color(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255), 0xFF)
          dataset("Group:$it") {
              items(50) {
                  SimpleLineData(
                      value = Random.nextInt(30, 100).toFloat(), color = groupColor
                  )
              }
          }
      }
  }
  
  // 2. use line chart
  LineChart(
      modifier = Modifier.height(240.dp),
      contentMeasurePolicy = fixedCrossAxisOverlayContentMeasurePolicy(32.dp.toPx()),
      chartDataset = dataset,
      tapGestures = TapGestures<LineData>().onTap { currentItem ->
          Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
      }
  )
  
  // curve line chart
  CurveLineChart(
      modifier = Modifier.height(240.dp),
      contentMeasurePolicy = fixedCrossAxisOverlayContentMeasurePolicy(32.dp.toPx()),
      chartDataset = dataset,
      tapGestures = TapGestures<LineData>().onTap { currentItem ->
          Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
      }
  )
  ```

### Bar Chart

  ```
  // 1. build dataset
  private fun buildChartDataset(): ChartDataset<BarData> {
      return chartDataGroup {
          repeat(3) { chartIndex ->
              dataset("Group:$chartIndex") {
                  items(50) {
                      SimpleBarData(
                          value = 10 + Random.nextInt(10, 50).toFloat(),
                          color = Color(
                              Random.nextInt(0, 255),
                              Random.nextInt(0, 255),
                              Random.nextInt(0, 255),
                              0xFF
                          )
                      )
                  }
              }
          }
      }
  }
  
  // 2. bar chart
  BarChart(
      modifier = Modifier.height(240.dp),
      contentMeasurePolicy = fixedCrossAxisContentMeasurePolicy(32.dp.toPx(), 8.dp.toPx(), 16.dp.toPx()),
      chartDataset = barDataset,
      tapGestures = TapGestures<BarData>().onTap { currentItem ->
          Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
      }
  )

  // 3. stack bar chart
  BarChart(
      modifier = Modifier.height(240.dp),
      contentMeasurePolicy = fixedCrossAxisOverlayContentMeasurePolicy(32.dp.toPx(), 8.dp.toPx()),
      barStyle = BarStyle.Stack,
      chartDataset = barDataset,
      tapGestures = TapGestures<BarData>().onTap { currentItem ->
          Toast.makeText(context, "onTap:${currentItem}", Toast.LENGTH_SHORT).show()
      }
  )
  ```
