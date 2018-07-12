package org.ditw.learning.mlgen.gd

import java.util.Random

import javax.swing.{JFrame, JPanel, WindowConstants}
import org.jfree.chart.{ChartFactory, ChartPanel, JFreeChart}
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.xy.{XYSeries, XYSeriesCollection}
import org.joda.time.DateTime

object PlotDataSet extends App {

  val rand = new Random(DateTime.now.getMillis)
  def genData(count:Int):Array[Double] = {
    val res = new Array[Double](count)
    res.indices.foreach(idx => res(idx) = rand.nextGaussian())
    res
  }

  def trace(d:Array[Double]):Unit = {
    println(d.map(dd => f"$dd%.3f").mkString(","))
  }

  trace(genData(10))

  def y(x:Double):Double = x*x + x + 3.25

  val xs = (0 to 100).map(x => x*0.5-25)
  val ys = xs.indices.map(idx => y(xs(idx))+rand.nextGaussian()*2)
  val series = new XYSeries("tt")
  xs.indices.foreach(idx => series.add(xs(idx), ys(idx)))

  val c: XYSeriesCollection = new XYSeriesCollection
  c.addSeries(series)
  c.addSeries(gd1(20.0, 1))

  def gd1(initVal:Double, learningRate:Double):XYSeries = {
    val res = new XYSeries("gd")
    def dy(x:Double):Double = 2*x+1

    val tolerance = 1e-8

    var x0 = initVal
    var y0 = y(x0)
    res.add(x0, y0)
    var delta = Double.MaxValue

    while (delta > tolerance) {
      val x1 = x0-learningRate*dy(x0)
      val y1 = y(x1)
      delta = math.abs(y1-y0)
      x0 = x1
      y0 = y1

      res.add(x0, y0)
      println(s"trying $x1 ...")
    }
    res
  }

  val title = "title"
  val xAxisLabel = "xAxisLabel"
  val yAxisLabel = "yAxisLabel"
  val orientation = PlotOrientation.VERTICAL
  val legend = false
  val tooltips = false
  val urls = false
  val chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel, c, orientation, legend, tooltips, urls)
  val panel = new ChartPanel(chart)

  val f = new JFrame
  f.add(panel)
  f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  f.pack()
  f.setTitle("Training Data")

  f.setVisible(true)

}
