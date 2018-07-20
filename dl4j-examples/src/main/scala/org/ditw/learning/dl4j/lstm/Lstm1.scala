package org.ditw.learning.dl4j.lstm

import java.util

import org.deeplearning4j.nn.conf.{BackpropType, NeuralNetConfiguration}
import org.deeplearning4j.nn.conf.layers.{LSTM, RnnOutputLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.dataset.api.DataSetPreProcessor
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.learning.config.RmsProp
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

object Lstm1 extends App {

  val data = (0 to 100).map(_*0.01).toArray
  val dataIter = new DataSetIterator() {
    private val d:Array[Double] = data
    private var currIdx = 0

    override def asyncSupported(): Boolean = false

    override def next(): DataSet = next(1)

    override def hasNext: Boolean = { currIdx < d.length }

    override def next(num:Int):DataSet = {
      val input = Nd4j.create(Array(1, 1, 1), 'f')
      val label = Nd4j.create(Array(1, 1, 1), 'f')
      input.putScalar(Array(0, 0, 0), d(currIdx))
      label.putScalar(Array(0, 0, 0), d(currIdx))
      currIdx += 1
      return new DataSet(input, label)
    }

    override def reset(): Unit = {
      currIdx = 0
    }

    override def batch(): Int = 1

    override def totalExamples(): Int = d.length

    override def numExamples(): Int = totalExamples

    override def inputColumns(): Int = 1

    override def getLabels: util.List[String] = throw new UnsupportedOperationException("Not implemented")

    override def setPreProcessor(dataSetPreProcessor: DataSetPreProcessor): Unit = throw new UnsupportedOperationException("Not implemented")

    override def getPreProcessor: DataSetPreProcessor = throw new UnsupportedOperationException("Not implemented")

    override def resetSupported(): Boolean = true

    override def totalOutcomes(): Int = 1

    override def cursor(): Int = currIdx
  }

  val lstmSize = 20
  val lookBack = 1

  val conf = new NeuralNetConfiguration.Builder()
    .seed(1234)
    .l2(0.001)
    .weightInit(WeightInit.XAVIER)
    .updater(new RmsProp(0.01))
    .list()
    .layer(0,
      new LSTM.Builder()
        .nIn(1)
        .nOut(lstmSize)
        .activation(Activation.TANH)
        .build()
    )
    .layer(1,
      new RnnOutputLayer.Builder(LossFunction.SQUARED_LOSS)
        .activation(Activation.IDENTITY)
        .nIn(lstmSize)
        .nOut(1)
        .build()
    )
    .backpropType(BackpropType.TruncatedBPTT)
    .tBPTTBackwardLength(lookBack)
    .tBPTTForwardLength(2)
    .pretrain(false)
    .backprop(true)
    .build()

  val net = new MultiLayerNetwork(conf)

  net.init()
  net.setListeners(new ScoreIterationListener(5))

  net.getLayers.foreach { l =>
    val nParams = l.numParams()
    println(s"Param # at layer ${l.getIndex}: $nParams")
  }

  val epochs = 100

  (0 until epochs).foreach { ep =>
    while (dataIter.hasNext) {
      val ds = dataIter.next()
      net.fit(ds)
    }

    dataIter.reset()
  }

  net.rnnClearPreviousState()

  val samples = 1
  val initInput = Nd4j.zeros(samples, 1, lookBack)
  (0 until lookBack).foreach { i =>
    (0 until samples).foreach { j =>
      initInput.putScalar(Array(j, 0, i), data(data.length-lookBack+i))
    }
  }
  var output = net.rnnTimeStep(initInput)
  output = output.tensorAlongDimension(output.size(2)-1, 1, 0)

  (0 until 100).foreach { _ =>
    val nextInput = Nd4j.zeros(1, 1, 1)
    val o = output.getDouble(0, 0)
    nextInput.putScalar(Array(0, 0, 0), o)
    output = net.rnnTimeStep(nextInput)
    output = output.tensorAlongDimension(output.size(2)-1, 1, 0)
    println(o)
  }

  println("done")
}
