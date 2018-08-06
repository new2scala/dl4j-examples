package org.ditw.learning.akkastr

import java.io.{File, FileInputStream}
import java.nio.charset.StandardCharsets

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.apache.commons.io.IOUtils
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

object FileStrmTest extends App {

  val folder = "/media/sf_vmshare/nonDottedInitials6"
  val files = new File(folder).listFiles().map(_.getAbsolutePath).toIndexedSeq
  val fileSource:Source[String, NotUsed] = Source(files)

  implicit val system = ActorSystem("filesys")
  implicit val mat = ActorMaterializer()

  val done:Future[Done] = fileSource.runWith(
    Sink.foreach { f =>
      val fstr = new FileInputStream(f)
      val fl = IOUtils.lineIterator(fstr, StandardCharsets.UTF_8).next()
      println(s"$f:\t$fl")
      fstr.close()
    }
  )

  done.onComplete(_ => system.terminate())

}
