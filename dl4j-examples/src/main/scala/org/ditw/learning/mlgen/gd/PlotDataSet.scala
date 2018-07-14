package org.ditw.learning.mlgen.gd

import java.awt.Color
import java.util
import java.util.Random

import javafx.scene.chart.NumberAxis
import javax.swing.{JFrame, JPanel, WindowConstants}
import org.jfree.chart.{ChartFactory, ChartPanel, JFreeChart}
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.data.xy.{XYDataset, XYSeries, XYSeriesCollection}
import org.jfree.ui.RefineryUtilities
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

  import GDExample1._

  val series = new XYSeries("tt")
  xs.indices.foreach(idx => series.add(xs(idx), ys(idx)))

  val c: XYSeriesCollection = new XYSeriesCollection
  c.addSeries(series)
//  val s2 = gd1(30.0, 0.2)
//  c.addSeries(s2)

  def gd1(initVal:Double, learningRate:Double):XYSeries = {
    val res = new XYSeries("gd", false)
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

  def createChart(ds:XYDataset):JFreeChart = {
    val chart = ChartFactory.createXYLineChart(
      "Line and dot",
      "X",
      "Y",
      ds,
      PlotOrientation.VERTICAL,
      true,
      true,
      false
    )

    chart.setBackgroundPaint(Color.white)

    val plot = chart.getXYPlot
    plot.setBackgroundPaint(Color.lightGray)

    plot.setDomainGridlinePaint(Color.white)
    plot.setRangeGridlinePaint(Color.white)

    val renderer = new XYLineAndShapeRenderer()

    renderer.setSeriesLinesVisible(0, false)
    renderer.setSeriesLinesVisible(1, true)
    plot.setRenderer(renderer)

//    val rangeAxis = plot.getRangeAxis()
//    rangeAxis.setStandardTickUnits(NumberAxis.)
    chart
  }

  val ch = createChart(c)

  val panel = new ChartPanel(ch)
  val f = new JFrame
  f.setContentPane(panel)

  f.pack()
  RefineryUtilities.centerFrameOnScreen(f)
  f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
  f.setVisible(true)

//  val title = "title"
//  val xAxisLabel = "xAxisLabel"
//  val yAxisLabel = "yAxisLabel"
//  val orientation = PlotOrientation.VERTICAL
//  val legend = false
//  val tooltips = false
//  val urls = false
//  val chart = ChartFactory.createScatterPlot(title, xAxisLabel, yAxisLabel, c, orientation, legend, tooltips, urls)
//  val panel = new ChartPanel(chart)
//
//  val s2 = gd2(20.0, 1)
//  val lineChart = ChartFactory.createLineChart(
//    "gd",
//    xAxisLabel, yAxisLabel,
//    s2,
//    PlotOrientation.VERTICAL,
//    true,true,false)

//  val panel2 = new ChartPanel(lineChart)
//
//  def gd2(initVal:Double, learningRate:Double):DefaultCategoryDataset = {
//    val res = new DefaultCategoryDataset
//    def dy(x:Double):Double = 2*x+1
//
//    val tolerance = 1e-8
//
//    var x0 = initVal
//    var y0 = y(x0)
//    res.addValue(x0, "s", y0)
//    var delta = Double.MaxValue
//
//    while (delta > tolerance) {
//      val x1 = x0-learningRate*dy(x0)
//      val y1 = y(x1)
//      delta = math.abs(y1-y0)
//      x0 = x1
//      y0 = y1
//
//      res.addValue(x0, "s", y0)
//      println(s"trying $x1 ...")
//    }
//    res
//  }
//
//  val f = new JFrame
//  f.add(panel2)
//  f.add(panel)
//  f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
//  f.pack()
//  f.setTitle("Training Data")
//
//  f.setVisible(true)

}
