package org.ditw.learning.thermoapp

import org.json4s.{DefaultFormats, FieldSerializer}

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


  import FieldSerializer._
//  private val rename = FieldSerializer[OneDriveFolderItem](
//    renameTo("@microsoft.graph.downloadUrl", "downloadUrl"),
//    renameFrom("downloadUrl", "@microsoft.graph.downloadUrl")
//  )

  def parseOneDriveItemJson(j:String):FolderResp = {
    import org.json4s.jackson.JsonMethods._
    implicit val fmt = DefaultFormats //+ rename
    parse(j).extract[FolderResp]
  }
}
