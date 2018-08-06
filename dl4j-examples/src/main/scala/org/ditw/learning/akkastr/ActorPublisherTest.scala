package org.ditw.learning.akkastr

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream._
import akka.stream.scaladsl.Source
import akka.stream.stage.{GraphStage, GraphStageLogic, OutHandler}

import concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object ActorPublisherTest extends App {

  class NumbersSource extends GraphStage[SourceShape[Int]] {
    val out:Outlet[Int] = Outlet("NumbersSource")

    override def shape: SourceShape[Int] = SourceShape(out)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
      new GraphStageLogic(shape) {
        private var counter = 1
        setHandler(out, new OutHandler {
          override def onPull(): Unit = {
            push(out, counter)
            counter += 1
          }
        })
      }
  }

  implicit val system = ActorSystem("Publisher")
  implicit val matr = ActorMaterializer()

  val sourceGraph:Graph[SourceShape[Int], NotUsed] = new NumbersSource
  val mySrc:Source[Int, NotUsed] = Source.fromGraph(sourceGraph)

  val result1:Future[Int] = mySrc.take(10).runFold(0)(_+_)
  result1.map(println)
  val result2:Future[Int] = mySrc.take(100).runFold(0)(_+_)
  result2.map(println)


}
