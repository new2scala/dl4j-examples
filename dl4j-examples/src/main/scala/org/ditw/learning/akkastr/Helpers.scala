package org.ditw.learning.akkastr

import org.deeplearning4j.examples.recurrent.processnews.CountryIteratorSkip
import org.nd4j.linalg.dataset.DataSet

import scala.concurrent.{Await, Future}
import concurrent.ExecutionContext.Implicits.global
import concurrent.duration._

object Helpers {

  import java.lang.{Integer => JavaInt}
  import java.util.{List => JavaList}
  //type DataSetFunc = (Int, Int, JavaList[JavaInt], JavaList[String]) => DataSet
  def asyncRunJ(batches:Int, batchSize:Int, cats:JavaList[JavaInt], catData:JavaList[String], countryIt:CountryIteratorSkip):Array[DataSet] = {
    val fr = Future.sequence(
      (0 until batches)
        .map(_*batchSize)
        .map(startIdx => Future(countryIt.nextDataSet(batchSize, startIdx, cats, catData)))
    )

    Await.result(fr, 60 seconds).toArray
  }

}
