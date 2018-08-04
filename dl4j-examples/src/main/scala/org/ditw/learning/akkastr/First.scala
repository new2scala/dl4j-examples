package org.ditw.learning.akkastr

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object First extends App {
  val source:Source[Int, NotUsed] = Source(1 to 100)

  val factorials:Source[BigInt, NotUsed] = source.scan(BigInt(1))(_*_)

  implicit val system = ActorSystem("QS")

  implicit val materializer = ActorMaterializer()

  val done:Future[Done] =
    //source.runForeach(println)
    factorials.map(num => s"$num\n")
        .runForeach(println)

  done.onComplete(_ => system.terminate())


}
