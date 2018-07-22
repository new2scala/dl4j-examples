package org.ditw.learning.dl4j.lstm.movement

import org.ditw.learning.dl4j.lstm.movement.DataGen._

import scala.util.Random

object DataGenTest extends App {

  val d = Seq(
    Array(1.0, 1.1, 1.2, 0.9),
    Array(1.3, 1.1, 1.2, 0.9),
    Array(1.2, 1.1, 1.2, 0.9),
    Array(1.20000001, 1.1, 1.2, 0.9),
    Array(1.0, 0.9, 1.0, 0.9),
    Array(1.0000000001, 0.899999999999, 1.0, 0.9)
  )

  d.foreach { dd =>
    //val md = MData(d)

    println(typ(dd))
  }

  println("t2")
  println(typ2(Seq(d(0), d(1))))
  println(typ2(Seq(
    Array(1.0, 1.1, 1.2, 0.9),
    Array(1.2, 1.1, 1.0, 0.9)
  )))
  println(typ2(Seq(
    Array(1.0, 1.2, 1.2, 1.1),
    Array(1.2, 1.0, 1.0, 1.1)
  )))

//  val rand = genRaw(10, 1.0, new Random())
//  println(rand)
//
//  val dataGen = genData(10, 1, new Random())
//  println(dataGen)

  val lookback = 1
  val steps = 30
//  genData2Files(steps, lookback, "/home/dev/tmp/lstm1/train", 4500)
//  genData2Files(steps, lookback, "/home/dev/tmp/lstm1/test", 1500)
  genData2Files(steps, lookback, "/home/dev/tmp/lstm1/t2", 20)
}
