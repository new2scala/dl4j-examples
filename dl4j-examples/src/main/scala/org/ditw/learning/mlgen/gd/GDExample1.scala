package org.ditw.learning.mlgen.gd

import java.util
import java.util.Random

import scala.collection.mutable.ListBuffer

object GDExample1 extends App {
  def y(x:Double):Double =
    x*x + x + 3.25


  val xs = (0 to 100).map(x => x*0.5-25)
  //val ys = xs.indices.map(idx => y(xs(idx))+rand.nextGaussian()*2)
  val ys = xs.indices.map(idx => y(xs(idx)))

  // ( (a0*x^2 + a1*x + a2) - y )^2
  def error(params:Array[Double], xs:Array[Double], ys:Array[Double]):Double = {
    def v(a1:Double, a2:Double, a3:Double, x:Double):Double = a1*x*x + a2*x + a3
    xs.indices.map { idx =>
      val x = xs(idx)
      val d = v(params(0), params(1), params(2), x) - ys(idx)
      d*d
    }.sum
  }

  // d/d_a0 ( (a0*x^2 + a1*x + a2) - y )^2 = 2*a0*x^4 + 2*x^2((a1*x+a2)-y)
  def errorD_a0(params:Array[Double], x:Double, y:Double):Double = {
    val (a0, a1, a2) = (params(0), params(1), params(2))

    2*a0*math.pow(x, 4) + 2*x*x*(a1*x+a2-y)
  }

  // d/d_a1 ( (a0*x^2 + a1*x + a2) - y )^2 = 2*a1*x^2 + 2*x((a0*x^2+a2)-y)
  def errorD_a1(params:Array[Double], x:Double, y:Double):Double = {
    val (a0, a1, a2) = (params(0), params(1), params(2))
    2*a1*x*x + 2*x*(a0*x*x+a2-y)
  }

  // d/d_a2 ( (a0*x^2 + a1*x + a2) - y )^2 = 2*a2 + 2*((a0*x^2+a1*x)-y)
  def errorD_a2(params:Array[Double], x:Double, y:Double):Double = {
    val (a0, a1, a2) = (params(0), params(1), params(2))
    2*a2 + 2*(a0*x*x+a1*x-y)
  }

  def gdBatch(params:Array[Double], xs:Array[Double], ys:Array[Double]):Array[Double] = {
    val res = Array(0.0, 0.0, 0.0)
    xs.indices.foreach { idx =>
      res(0) += errorD_a0(params, xs(idx), ys(idx))
      res(1) += errorD_a1(params, xs(idx), ys(idx))
      res(2) += errorD_a2(params, xs(idx), ys(idx))
    }
    res.map(r => r/xs.size)
  }

  def shuffle[T](len:Int, arrs: Array[T]*): Unit = {
    val n = len
    val random = new Random
    // Loop over array.
    (0 until len).foreach { idx =>
      val randomValue = idx + random.nextInt(n - idx)
      arrs.foreach { arr =>
        val rElem = arr(randomValue)
        arr(randomValue) = arr(idx)
        arr(idx) = rElem
      }
    }
  }
  def break2Batches(batchSize:Int, fullDataSize:Int, arrs: Array[Double]*):List[Seq[Array[Double]]] = {
    val res = ListBuffer[Seq[Array[Double]]]()

    var idx = 0
    while (idx < fullDataSize) {
      val maxIdx = if (idx + batchSize > fullDataSize) fullDataSize else idx+batchSize

      val r = ListBuffer[Array[Double]]()
      arrs.foreach { arr =>
        r += util.Arrays.copyOfRange(arr, idx, maxIdx)
      }
      res += r
      idx += batchSize
    }

    res.toList
  }
  //  def sgdMiniBatch(params:Array[Double], xs:Array[Double], ys:Array[Double]):Array[Double] = {
  //
  //  }

  def sgd(params:Array[Double], x:Double, y:Double):Array[Double] = {
    val res = Array(
      errorD_a0(params, x, y),
      errorD_a1(params, x, y),
      errorD_a2(params, x, y)
    )
    res.map(r => r/xs.size)
  }

  def fitMiniBatch(
    initParams:Array[Double],
    xs:Array[Double],
    ys:Array[Double],
    learningRate:Double,
    epochs:Int,
    miniBatchSize:Int = 32
    //gd:(Array[Double], Array[Double], Array[Double]) => Array[Double]
  ):Unit = {
    var params = util.Arrays.copyOf(initParams, initParams.length)

    (0 until epochs).foreach { ep =>

      shuffle(xs.length, xs, ys)

      val batches = break2Batches(miniBatchSize, xs.length, xs, ys)
      batches.foreach { batch =>
        val bxs = batch(0)
        val bys = batch(1)
        val gdr = gdBatch(params, bxs, bys)
        params = params.indices.map { idx =>
          params(idx) - learningRate * gdr(idx)
        }.toArray

        val paramTr = params.map(p => f"$p%.5f").mkString(",")

        val err = error(params, xs, ys)
        println(
          f"$paramTr:\t $err%.5f"
        )
      }

      println(s"epoch: $ep")
    }
  }

  def fitMiniBatch_Momentum(
                    initParams:Array[Double],
                    xs:Array[Double],
                    ys:Array[Double],
                    learningRate:Double,
                    momentum:Double,
                    epochs:Int,
                    miniBatchSize:Int = 32
                    //gd:(Array[Double], Array[Double], Array[Double]) => Array[Double]
                  ):Unit = {
    var params = util.Arrays.copyOf(initParams, initParams.length)

    val momentumV = Array(0.0, 0.0, 0.0)
    (0 until epochs).foreach { ep =>

      shuffle(xs.length, xs, ys)

      val batches = break2Batches(miniBatchSize, xs.length, xs, ys)
      batches.foreach { batch =>
        val bxs = batch(0)
        val bys = batch(1)
        val gdr = gdBatch(params, bxs, bys)
        params = params.indices.map { idx =>
          momentumV(idx) = momentum*momentumV(idx) + learningRate * gdr(idx)
          params(idx) - momentumV(idx)
        }.toArray

        val paramTr = params.map(p => f"$p%.5f").mkString(",")

        val err = error(params, xs, ys)
        println(
          f"$paramTr:\t $err%.5f"
        )
      }

      println(s"epoch: $ep")
    }
  }

  def fit(
           initParams:Array[Double],
           xs:Array[Double],
           ys:Array[Double],
           learningRate:Double,
           epochs:Int,
           gd:(Array[Double], Array[Double], Array[Double]) => Array[Double]
         ):Unit = {
    var params = util.Arrays.copyOf(initParams, initParams.length)

    (0 until epochs).foreach { _ =>

      val err = error(params, xs, ys)
      val gdr = gd(params, xs, ys)
      params = params.indices.map { idx =>
        params(idx) - learningRate * gdr(idx)
      }.toArray

      val paramTr = params.map(p => f"$p%.5f").mkString(",")

      println(
        f"$paramTr:\t $err%.5f"
      )
    }
  }
  def fit_sgd(
               initParams:Array[Double],
               xs:Array[Double],
               ys:Array[Double],
               learningRate:Double,
               epochs:Int
             ):Unit = {
    var params = util.Arrays.copyOf(initParams, initParams.length)

    (0 until epochs).foreach { ep =>

      xs.indices.foreach { idx =>
        val gdr = sgd(params, xs(idx), ys(idx))
        params = params.indices.map { idx =>
          params(idx) - learningRate * gdr(idx)
        }.toArray

        val paramTr = params.map(p => f"$p%.5f").mkString(",")

        val err = error(params, xs, ys)
        println(
          f"$paramTr:\t $err%.5f"
        )
      }

      println(s"epoch: $ep")
    }
  }

  var initParams = Array(0.0, 0.0, 0.0)
  fitMiniBatch_Momentum(initParams, xs.toArray, ys.toArray, 0.000006, 0.9,10000, 16)
  initParams = Array(0.0, 0.0, 0.0)
  fitMiniBatch(initParams, xs.toArray, ys.toArray, 0.00001, 10000, 16)
//  initParams = Array(0.0, 0.0, 0.0)
//  fit_sgd(initParams, xs.toArray, ys.toArray, 0.00001, 500)
  initParams = Array(0.0, 0.0, 0.0)
  fit(initParams, xs.toArray, ys.toArray, 0.00001, 10000, gdBatch)
  println("ok")

}
