package org.ditw.learning.dl4j.feed4wd

import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.sentenceiterator.{BasicLineIterator, SentenceIterator}
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.{DefaultTokenizerFactory, TokenizerFactory}

object WVec2 extends App {

  val path = "/media/sf_vmshare/germany.txt"

  val iter = new BasicLineIterator(path)
  val t = new DefaultTokenizerFactory
  t.setTokenPreProcessor(new CommonPreprocessor)

  val wv:Word2Vec = new Word2Vec.Builder()
    .minWordFrequency(5)
    .iterations(1)
    .layerSize(50)
    .seed(42)
    .windowSize(5)
    .iterate(iter)
    .tokenizerFactory(t)
    .build()

  wv.fit()

  import collection.JavaConverters._

  val testWords = List(
    "Division",
    "Department",
    "Urology",
    "Surgery"
  ).map(_.toLowerCase())

  testWords.foreach { w =>
    val r = wv.wordsNearest(w, 10)
    println(r)
  }
}
