package org.ditw.learning.akkastr

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import scala.concurrent.Future

object Ex3 extends App {
  val source:Source[Int, NotUsed] = Source(1 to 100)

  val factorials:Source[BigInt, NotUsed] = source.scan(BigInt(1))(_*_)

  implicit val system = ActorSystem("QS")

  implicit val materializer = ActorMaterializer()

  val done:Future[Done] =
  //source.runForeach(println)
    factorials
      .zipWith(source)((res, num) => s"$num! = $res")
      .throttle(10, 1 second)
      .runForeach(println)

  done.onComplete(_ => system.terminate())

}
