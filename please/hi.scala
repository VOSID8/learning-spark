  stage = new JFXApp.PrimaryStage {
  title = "Temp Plot"
  scene = new Scene(500, 500) {
    val xAxis = NumberAxis()
    val yAxis = NumberAxis()
    val pData = XYChart.Series[Int, Int]("Data",
      ObservableBuffer(XYChart.Data(1, 2),XYChart.Data(2, 3),XYChart.Data(3, 4),XYChart.Data(4, 5))
    )
    val plot = new ScatterChart(xAxis, yAxis, ObservableBuffer(pData))
    root = plot
    }
  }



  object ScatterChartExample extends JFXApp {
  val xAxis = NumberAxis()
  val yAxis = NumberAxis()
  val scatterChart = new ScatterChart(xAxis, yAxis)

  val Data = ObservableBuffer(
    
  )

  val series = XYChart.Series[Int,Int]("Series 1", Data)
  scatterChart.getData.add(series)

  stage = new JFXApp.PrimaryStage {
    title = "Scatter Chart Example"
    scene = new Scene(500, 500) {
      root = scatterChart
    }
  }


}