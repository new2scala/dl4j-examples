package org.ditw.learning.dl4j.cnn

import java.io.File
import java.util.Random

import org.datavec.api.io.labels.ParentPathLabelGenerator
import org.datavec.api.split.FileSplit
import org.datavec.image.loader.NativeImageLoader
import org.datavec.image.recordreader.ImageRecordReader
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator
import org.deeplearning4j.examples.utilities.DataUtilities
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers.{ConvolutionLayer, DenseLayer, OutputLayer, SubsamplingLayer}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler
import org.nd4j.linalg.learning.config.Nesterovs
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction

object CNNEx1 extends App {
  val (h, w) = (28, 28)

  // gray
  val channels = 1

  val outputNum = 10

  val batchSize = 32
  val nEpoch = 1

  val iterations = 1

  val seed = 2419

  private val basePath = System.getProperty("java.io.tmpdir") + "/mnist"
  private val dataUrl = "http://github.com/myleott/mnist_png/raw/master/mnist_png.tar.gz"

  println("Downloading data")
  private val localFilePath = s"$basePath/mnist_png.tar.gz"
  if (DataUtilities.downloadFile(dataUrl, localFilePath))
    println(s"data downloaded from $dataUrl")

  if (!new File(s"$basePath/mnist_png").exists())
    DataUtilities.extractTarGz(localFilePath, basePath)

  val trainDataDir = new File(s"$basePath/mnist_png/training")
  val randNumGen = new Random(seed)
  val trainSplit = new FileSplit(trainDataDir, NativeImageLoader.ALLOWED_FORMATS, randNumGen)

  val labelMaker = new ParentPathLabelGenerator
  val trainRR = new ImageRecordReader(h, w, channels, labelMaker)

  trainRR.initialize(trainSplit)

  val trainIter = new RecordReaderDataSetIterator(trainRR, batchSize, 1, outputNum)
  val scaler = new ImagePreProcessingScaler(0, 1)
  scaler.fit(trainIter)
  trainIter.setPreProcessor(scaler)

  val testDataDir = new File(s"$basePath/mnist_png/testing")
  val testSplit = new FileSplit(testDataDir, NativeImageLoader.ALLOWED_FORMATS, randNumGen)
  val testRR = new ImageRecordReader(h, w, channels, labelMaker)
  testRR.initialize(testSplit)
  val testIter = new RecordReaderDataSetIterator(testRR, batchSize, 1, outputNum)
  testIter.setPreProcessor(scaler)

  val conf = new NeuralNetConfiguration.Builder()
    .seed(seed)
    .l2(0.0005)
    .updater(new Nesterovs(0.001, 0.9))
    .weightInit(WeightInit.XAVIER)
    .list()
    .layer(0,
      new ConvolutionLayer.Builder(5, 5)
        .nIn(channels)
        .stride(1, 1)
        .nOut(20)
        .activation(Activation.IDENTITY)
        .build()
    )
    .layer(1,
      new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
        .kernelSize(2, 2)
        .stride(2, 2)
        .build()
    )
    .layer(2,
      new ConvolutionLayer.Builder(5, 5)
        .stride(1, 1)
        .nOut(50)
        .activation(Activation.IDENTITY)
        .build()
    )
    .layer(3,
      new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
        .kernelSize(2, 2)
        .stride(2, 2)
        .build()
    )
    .layer(4,
      new DenseLayer.Builder().activation(Activation.RELU)
        .nOut(500)
        .build()
    )
    .layer(5,
      new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD)
        .nOut(outputNum)
        .activation(Activation.SOFTMAX)
        .build()
    )
    .setInputType(
      InputType.convolutional(28, 28, 1)
    )
    .backprop(true)
    .pretrain(false)
    .build()

  val net = new MultiLayerNetwork(conf)
  net.init()
  net.setListeners(new ScoreIterationListener(10))
  println(net.numParams())

  (0 until nEpoch).foreach { ep =>
    net.fit(trainIter)

    val eval = net.evaluate(testIter)

    println(
      s"Epoch $ep\n${eval.stats()}"
    )
    trainIter.reset()
    testIter.reset()

  }
}
