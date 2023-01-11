package sparksql

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.types.StringType
import org.apache.spark.sql.types.DateType
import org.apache.spark.sql.types.DoubleType
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._
import scalafx.scene.Scene
import scalafx.scene.chart.ScatterChart
import scalafx.collections.ObservableBuffer
import scalafx.scene.chart.NumberAxis
import scalafx.scene.chart.Axis
import scalafx.scene.chart.ValueAxis
import scalafx.scene.chart.XYChart
import scalafx.application.JFXApp
import scalafx.scene.chart.XYChart.Data
import scalafx.scene.paint.Color

/*
 * NOAA data from ftp://ftp.ncdc.noaa.gov/pub/data/ghcn/daily/  in the by_year directory
 */

object NOAAData extends JFXApp {
  val spark = SparkSession.builder().master("local[*]").appName("NOAA Data").getOrCreate()
  import spark.implicits._
  
  spark.sparkContext.setLogLevel("WARN")
  
  val tschema = StructType(Array(
      StructField("sid",StringType),
      StructField("date",DateType),
      StructField("mtype",StringType),
      StructField("value",DoubleType)
      ))
  val data2017 = spark.read.schema(tschema).option("dateFormat", "yyyyMMdd").csv("src/main/resources/2022.csv").cache()
//  data2017.show()
//  data2017.schema.printTreeString()
  
  val sschema = StructType(Array(
      StructField("sid", StringType),
      StructField("lat", DoubleType),
      StructField("lon", DoubleType),
      StructField("name", StringType)
      ))
  val stationRDD = spark.sparkContext.textFile("src/main/resources/ghcnd-stations.txt").map { line =>
    val id = line.substring(0, 11)
    val lat = line.substring(12, 20).toDouble
    val lon = line.substring(21, 30).toDouble
    val name = line.substring(41, 71)
    Row(id, lat, lon, name)
  }
  val stations = spark.createDataFrame(stationRDD, sschema).cache()
  
  val tmax2017 = data2017.filter($"mtype" === "TMAX").limit(1000000).drop("mtype").withColumnRenamed("value", "tmax")
  val tmin2017 = data2017.filter('mtype === "TMIN").limit(1000000).drop("mtype").withColumnRenamed("value", "tmin")
  val combinedTemps2017 = tmax2017.join(tmin2017, Seq("sid", "date"))
  val dailyTemp2017 = combinedTemps2017.select('sid, 'date, ('tmax + 'tmin)/2 as "tave")
  val stationTemp2017 = dailyTemp2017.groupBy('sid).agg(avg('tave) as "tave")
  val joinedData2017 = stationTemp2017.join(stations, "sid")
  joinedData2017.show()
  val data = joinedData2017.map { line =>
    (line.getDouble(1),line.getDouble(2), line.getDouble(3))
  }.collect()

  stage = new JFXApp.PrimaryStage {
    title = "Temp Plot"
    scene = new Scene(500, 500) {
      val xAxis = NumberAxis()
      val yAxis = NumberAxis()
      val pData = XYChart.Series[Number, Number]("Temps", 
          ObservableBuffer(data.map(td => if(td._1 < 5){XYChart.Data[Number, Number](td._3, td._2,Color.Violet)}
          else if(td._1 < 10 && td._1 >=5){XYChart.Data[Number, Number](td._3, td._2,Color.Indigo)}
          else if(td._1 < 15 && td._1 >=10){XYChart.Data[Number, Number](td._3, td._2,Color.Blue)}
          else if(td._1 < 20 && td._1 >=15){XYChart.Data[Number, Number](td._3, td._2,Color.Green)}
          else if(td._1 < 25 && td._1 >=20){XYChart.Data[Number, Number](td._3, td._2,Color.Yellow)}
          else if(td._1 < 30 && td._1 >=25){XYChart.Data[Number, Number](td._3, td._2,Color.Orange)}
          else {XYChart.Data[Number, Number](td._3, td._2,Color.Red)}
          ): _*))
      val plot = new ScatterChart(xAxis, yAxis, ObservableBuffer(pData))
      root = plot
    }
  }






  // stage = new JFXApp.PrimaryStage {
    // title = "Temp Plot"
    // scene = new Scene(500, 500) {
      // val xAxis = NumberAxis()
      // val yAxis = NumberAxis()
      // val pData = Seq(joinedData2017.collect().map { row =>
        // XYChart.Data(row.getDouble(2), row.getDouble(3))
      // })
      // val plot = new ScatterChart(xAxis, yAxis, ObservableBuffer(pData))
      // root = plot
    // }
  // }

  spark.stop()
}
