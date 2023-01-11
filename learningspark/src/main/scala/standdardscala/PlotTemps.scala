package standardscala

import scalafx.application.JFXApp
import scalafx.scene.chart.ScatterChart
import scalafx.collections.ObservableBuffer
import scalafx.scene.chart.NumberAxis
import scalafx.scene.chart.XYChart
import swiftvis2.plotting
import swiftvis2.plotting.Plot
import swiftvis2.plotting.renderer.FXRenderer
import swiftvis2.plotting.ColorGradient
import swiftvis2.plotting.styles._
import swiftvis2.plotting.renderer.SwingRenderer
import swiftvis2.plotting._
import swiftvis2.plotting.renderer.Renderer
import swiftvis2.plotting.Plot.GridData

object PlotTemps extends JFXApp {  
  val source = scala.io.Source.fromFile("src/main/resources/MN212142_9392.csv")
  val lines = source.getLines().drop(1)
  val data = lines.flatMap { line =>
    val p = line.split(",")
    if (p(7) == "." || p(8) == "." || p(9) == ".") Seq.empty else
      Seq(TempData(p(0).toInt, p(1).toInt, p(2).toInt, p(4).toInt,
        TempData.toDoubleOrNeg(p(5)), TempData.toDoubleOrNeg(p(6)), p(7).toDouble, p(8).toDouble,
        p(9).toDouble))
  }.toArray
  source.close()


  val rainData = data.filter(_.precip >= 0.0).sortBy(_.precip)
  val plot = Plot.scatterPlot(rainData.map(_.doy), rainData.map(_.tmax), symbolColor=BlackARGB, 
  symbolSize=5,title="Hii",xLabel="radial",yLabel="coveredArea")
  SwingRenderer(plot, 800, 600, true)

}