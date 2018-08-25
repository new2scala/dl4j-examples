package org.ditw.learning

import java.io.FileInputStream
import java.nio.charset.StandardCharsets

import org.apache.commons.io.IOUtils

object GoergiaCleanup extends App {

  val inFile = "Y:\\vmshare\\fp2Affs-full\\curation\\204.txt"

  val wordSet = List(
    "tbilsi",
    "tbilissi"
//    "tbilisi",
//    "t'bilisi",
//    "batumi",
//    "kutaisi",
//    "rustavi"
//    "united states of america",
//    "united states",
//    "usa",
//    "u.s.a",
//    "estados unidos de américa"
  )
  val negSet = List(
    "atlanta",
    "athens",
    "emory university",
    "augusta"
  )
  import collection.JavaConverters._
  val out = IOUtils.readLines(new FileInputStream(inFile), StandardCharsets.UTF_8)
    .asScala.flatMap { l =>
    if (wordSet.exists(pos => l.contains(pos))) {
      Option(l)
//      var processed = false
//      val it = wordSet.iterator
//      var res:Option[String] = None
//      while (!processed && it.hasNext) {
//        val w = it.next()
//        if (l.endsWith(w)) {
//          val tr = l.substring(0, l.length-w.length).trim
//          res = Option(tr)
//        }
//      }
//      res
    }
    else
      None
  }

  out.distinct.sorted.foreach(println)

}
