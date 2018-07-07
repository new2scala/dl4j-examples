package org.ditw.learning.dl4j.feed4wd

import java.util

import org.deeplearning4j.nn.conf.layers.{DenseLayer, OutputLayer}
import org.deeplearning4j.nn.conf.{MultiLayerConfiguration, NeuralNetConfiguration}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.Nesterovs
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

object WVec1 extends App {
  val testSents = Array(
    "He is the king",
    "The king is royal",
    "She is royal queen"
  )

  val lowerTokens = testSents.map(_.split("\\s+").map(_.toLowerCase()))
  val allVoc = lowerTokens.flatten.toSet.toIndexedSeq

  val dict:Map[String, Int] = allVoc.indices.map(idx => allVoc(idx) -> (idx+1)).toMap

  println(dict)

  val windowSize = 2

  val pairs = lowerTokens.flatMap { sent =>
    for (
      i <- sent.indices;
      j <- i-windowSize to i+windowSize if (i!=j && j >= 0 && j < sent.length)
    ) yield sent(i) -> sent(j)
  }

  println(pairs)

  val encoded = pairs.map(p => dict(p._1) -> dict(p._2))
  println(encoded)

  def to1Hot(v:Int, dim:Int):Array[Double] = {
    val res = new Array[Double](dim)
    util.Arrays.fill(res, 0.0)
    res(v-1) = 1.0
    res
  }

  val enc1Hot = encoded
    .map { p => to1Hot(p._1, dict.size) -> to1Hot(p._2, dict.size)}

  println(enc1Hot)

  val embeddingDim = 5


  //val testIter = new DataSetInterator()

  val conf = new NeuralNetConfiguration.Builder()
    .seed(123)
    .updater(new Nesterovs(0.1, 0.9))
    .list()
    .layer(0,
      new DenseLayer.Builder()
        .nIn(dict.size)
        .nOut(embeddingDim)
        .weightInit(WeightInit.XAVIER).activation(Activation.IDENTITY).build
    )
    .layer(1,
      new DenseLayer.Builder()
        .nIn(embeddingDim)
        .nOut(dict.size)
        .weightInit(WeightInit.XAVIER).activation(Activation.IDENTITY).build
    )
    .layer(2,
      new OutputLayer.Builder(LossFunction.RECONSTRUCTION_CROSSENTROPY)
        .weightInit(WeightInit.XAVIER).activation(Activation.SOFTMAX)
        .nIn(dict.size)
        .nOut(dict.size)
        .build
    )
    .pretrain(false)
    .backprop(true)
    .build()

  val model = new MultiLayerNetwork(conf)

  model.init()
  model.setListeners(new ScoreIterationListener(10))


  val x_tr:INDArray = Nd4j.create(enc1Hot.map(_._1))
  val y_tr:INDArray = Nd4j.create(enc1Hot.map(_._2))
  val epochs = 400
  (0 until epochs).foreach( _ => model.fit(x_tr, y_tr))

  System.out.println("Evaluate model....")
  val indexedDict = dict.values.toArray.sorted.map(x => x -> to1Hot(x, dict.size)).toMap
  val dictIn = indexedDict.toArray.sortBy(_._1).map(_._2)
  val outputIn = model.reconstruct(Nd4j.create(dictIn), 2)
  println(outputIn)
  val indexedDictVec:Map[Int, INDArray] = indexedDict.keys.map(k => k -> outputIn.getRow(k-1)).toMap
  //model.pr

  val t1 = to1Hot(dict("royal"), dict.size)
  val x1 = Nd4j.create(t1)
  val r1 = model.predict(x1)
  println(dict)

  findWord("royal")
  findWord("he")
  findWord("she")
  findWord("king")
  findWord("queen")


  def findWord(w:String):Unit = {
    val v1 = indexedDictVec(dict(w))

    val res = dict.minBy { p =>
      if (p._2 == dict(w))
        Double.MaxValue
      else {
        val w1 = p._1
        val idx = p._2
        val vec = indexedDictVec(idx)
        val dist = v1.distance2(vec)
        dist
      }
    }

    println(res._1)
  }
}
