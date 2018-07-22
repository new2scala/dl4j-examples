package org.ditw.learning.dl4j.lstm.movement

import org.ditw.learning.dl4j.lstm.movement.DataGen.MData

object DataGenTest extends App {

  val d = List(
    Array(1.0, 1.1, 1.2, 0.9),
    Array(1.3, 1.1, 1.2, 0.9),
    Array(1.2, 1.1, 1.2, 0.9),
    Array(1.20000001, 1.1, 1.2, 0.9),
    Array(1.0, 0.9, 1.0, 0.9),
    Array(1.0000000001, 0.899999999999, 1.0, 0.9)
  )

  d.foreach { dd =>
    val md = MData(dd)

    println(md.t)
  }

}
