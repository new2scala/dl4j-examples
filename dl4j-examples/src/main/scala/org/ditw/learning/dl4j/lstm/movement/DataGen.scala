package org.ditw.learning.dl4j.lstm.movement

import java.io.{File, FileOutputStream}
import java.nio.charset.StandardCharsets

import org.apache.commons.io.IOUtils

import scala.util.Random

object DataGen {

  object MovType extends Enumeration {
    type MovType = Value
    val E, SE, S, SW, W, NW, N, NE, O = Value
  }

  import MovType._

  private[DataGen] val movTypeMap = Map(
    E -> 0,
    SE -> 1,
    S -> 2,
    SW -> 3,
    W -> 4,
    NW -> 5,
    N -> 6,
    NE -> 7,
    O -> 8
  )

  private[DataGen] val movTypeRefMap = Map(
    (None, None) -> O,
    (None, Option(S)) -> S,
    (None, Option(N)) -> N,
    (Option(E), None) -> E,
    (Option(W), None) -> W,
    (Option(W), Option(S)) -> SW,
    (Option(W), Option(N)) -> NW,
    (Option(E), Option(S)) -> SE,
    (Option(E), Option(N)) -> NE
  )

  // d: Array[4] - E S W N
  private val tol0 = 1e-4
  //case class MData(d:Array[Double])

  private[movement] def typ(delta:Array[Double]):MovType = {
    val deltaE = delta(0) - delta(2)
    val deltaS = delta(1) - delta(3)

    val mE =
      if (math.abs(deltaE) > tol0) {
        if (deltaE > 0) Option(E)
        else Option(W)
      }
      else None
    val mS =
      if (math.abs(deltaS) > tol0) {
        if (deltaS > 0) Option(S)
        else Option(N)
      }
      else None

    movTypeRefMap(mE -> mS)
  }

  private[movement] def typ2(deltas:Seq[Array[Double]]):MovType = {
    val s = new Array[Double](4)
    (0 to 3).foreach { idx =>
      s(idx) = deltas.map(_(idx)).sum
    }
    typ(s)
  }


  private[movement] def genRaw(steps:Int, max:Double, rand:Random):Array[Array[Double]] = {
    val res = new Array[Array[Double]](steps)
    res.indices.foreach { idx =>
      res(idx) = new Array[Double](4)
      (0 to 3).foreach { j =>
        res(idx)(j) = rand.nextDouble()*max
      }
    }
    res
  }

  private[movement] def rawToResult(raw:Array[Array[Double]], lookback:Int):Array[Int] = {
    val res = new Array[Int](raw.length)
    // todo: should be masked instead
//    (0 until lookback).foreach { idx =>
//      res(idx) = movTypeMap(O)
//    }
    (lookback until raw.length).foreach { idx =>
      res(idx) = movTypeMap(typ2(raw.slice(idx-lookback, idx+1).toSeq))
    }
    res
  }

  private[movement] def genData(steps:Int, lookback:Int, rand:Random, max:Double = 1.0):(Array[Array[Double]], Array[Int]) = {
    val raw = genRaw(steps, max, rand)
    val res = rawToResult(raw, lookback)
    raw -> res
  }

  private def mkdirsIfNotExist(f:File):Unit = {
    if (!f.exists()) {
      f.mkdirs()
    }

    if (!f.exists()) {
      throw new RuntimeException(s"Cannot create dir [${f.getAbsolutePath}]!")
    }
  }

  private val MovementDirName = "mm"
  private val LabelDirName = "lb"
  private[movement] def genData2Files(
    steps:Int,
    lookback:Int,
    rootDir:String,
    fileCount:Int,
    max:Double = 1.0,
    randSeed:Int = 1234
  ):Unit = {
    assert(fileCount <= 10000)
    val dir = new File(rootDir)

    mkdirsIfNotExist(dir)
    val movementDir = new File(dir, MovementDirName)
    mkdirsIfNotExist(movementDir)
    val labelDir = new File(dir, LabelDirName)
    mkdirsIfNotExist(labelDir)

    val rand = new Random(randSeed)

    (0 until fileCount).foreach { fidx =>
      val (mv, lb) = genData(steps, 1, rand)
      val mmData = mv.map(_.map(d => f"$d%.6f").mkString(",")).mkString("\n")
      val mmOutFile = new FileOutputStream(new File(movementDir, f"$fidx%04d.csv"))
      IOUtils.write(mmData, mmOutFile, StandardCharsets.UTF_8)
      mmOutFile.close()

      val lbData = lb.mkString("\n")
      val lbOutFile = new FileOutputStream(new File(labelDir, f"$fidx%04d.csv"))
      IOUtils.write(lbData, lbOutFile, StandardCharsets.UTF_8)
      lbOutFile.close()
    }
  }
}
