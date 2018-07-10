package org.ditw.learning.mlgen.gd

object GradientDescent1 extends App {

  // y = (x+0.5)^2 + 3
  // y' = 2x + 1
  // y_min = -0.5

  def y(x:Double):Double = x*x + x + 3.25

  def dy(x:Double):Double = 2*x+1

  val learningRate = 0.7 //1e-3

  val tolerance = 1e-8

  var x0 = 20.0d
  var y0 = y(x0)
  var delta = Double.MaxValue

  while (delta > tolerance) {
    val x1 = x0-learningRate*dy(x0)
    val y1 = y(x1)
    delta = math.abs(y1-y0)
    x0 = x1
    y0 = y1

    println(s"trying $x1 ...")
  }

  println(f"Found optimal: $x0%.3f")
}
