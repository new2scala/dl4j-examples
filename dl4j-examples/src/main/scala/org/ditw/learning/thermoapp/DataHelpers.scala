package org.ditw.learning.thermoapp

object DataHelpers {
  case class DataUnit(
    thermoGrid:Array[Array[Double]],
    imageRaw:Array[Byte]
  )

  trait DataSource {
    def curr:DataUnit
    def next:Option[DataUnit]
    def prev:Option[DataUnit]
  }
}
