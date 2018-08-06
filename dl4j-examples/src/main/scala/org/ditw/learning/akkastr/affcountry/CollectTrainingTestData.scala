package org.ditw.learning.akkastr.affcountry

import java.io.{File, FileInputStream}
import java.nio.charset.StandardCharsets

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.apache.commons.io.IOUtils
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import collection.JavaConverters._

object CollectTrainingTestData extends App {
  val folder = "/media/sf_vmshare/aff-w2v"
  val files = new File(folder).listFiles().map(_.getAbsolutePath).toIndexedSeq
  val fileSource:Source[String, NotUsed] = Source(files)

  implicit val system = ActorSystem("filesys")
  implicit val mat = ActorMaterializer()

//  val t1 = CountryData.sameCountryInLine(
//    Tokenizer.simpleTokenize(
//      "University of Cambridge Clinical School, Addenbrooke's Hospital, Hills Road, Cambridge CB2 0QQ, UK. USA Electronic address: mh623@cam.ac.uk.",
//      true)
//  )
//  println(t1)

  val done:Future[Done] = fileSource.runWith(
    Sink.foreach { f =>
      val fstr = new FileInputStream(f)
      val lines = IOUtils.readLines(fstr, StandardCharsets.UTF_8).asScala

      lines.foreach { l =>
        val tokens = Tokenizer.simpleTokenize(l, true)
        val countries = CountryData.sameCountryInLine(tokens, l)
//        if (!countries.isEmpty)
//          println(s"$countries(${tokens.length}): \t$l")
        countries
      }

    }
  )

  done.onComplete(_ => system.terminate())

}
