package org.ditw.learning

import java.io.{FileInputStream, FileOutputStream}
import java.nio.charset.StandardCharsets

import org.apache.commons.io.IOUtils

object ExcludeLines extends App {

  val inputFile = "Y:\\vmshare\\fp2Affs-w2v\\204.txt"
  val excludeLinesFile = "Y:\\vmshare\\fp2Affs-w2v\\train\\204.txt"
  val resFile = "Y:\\vmshare\\fp2Affs-w2v\\204-ex.txt"

  import collection.JavaConverters._
  val all = IOUtils.readLines(new FileInputStream(inputFile), StandardCharsets.UTF_8).asScala.toSet

  val toExclude = IOUtils.readLines(new FileInputStream(excludeLinesFile), StandardCharsets.UTF_8).asScala

  println(s"Before: ${all.size}")

  val res = (all -- toExclude.toSet).filter(!_.contains("tbilisi"))
  println(s" After: ${res.size}")

  IOUtils.write(
    res.mkString("\n"), new FileOutputStream(resFile), StandardCharsets.UTF_8
  )


}
