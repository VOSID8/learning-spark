
package sparkrdd

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import scalafx.application.JFXApp
object RDDTempData extends JFXApp {
  val conf = new SparkConf().setAppName("Temp Data").setMaster("local[*]")
  val sc = new SparkContext(conf)
  val lines = sc.textFile("src/main/resources/MN212142_9392.csv")

  println("Total Records: " + lines.count())
}
