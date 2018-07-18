package org.ditw.learning.sparkstrm

import org.apache.spark.SparkConf
import org.apache.spark.streaming._

object Test1 extends App {

  val conf = new SparkConf().setMaster("local[*]").setAppName("network word count")

  val ssc = new StreamingContext(conf, Seconds(10))

  val lines = ssc.socketTextStream("localhost", 9999)
  var acc = Map[String, Int]()

  val words = lines.flatMap(_.split(" ").map(_.toLowerCase()))
  val ps = words.map(_ -> 1)

  val wc = ps.reduceByKey(_ + _)

  wc.foreachRDD { rdd =>
    acc = (rdd.collect() ++ acc).groupBy(x => x._1).mapValues(_.map(_._2).sum)
    println(acc)
  }

  //println(acc)

  ssc.start()

  ssc.awaitTermination()

}
