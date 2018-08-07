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

  case class OneDriveFolderItem(
    name:String,
    //downloadUrl:String
    `@microsoft.graph.downloadUrl`:String
  )

  case class OneDriveFolderResp(
    value:Array[OneDriveFolderItem]
  ) {
    private val _downloadLinks = value.sortBy(_.name).map(_.`@microsoft.graph.downloadUrl`)
    def downloadLinks:Array[String] = _downloadLinks
  }

  import FieldSerializer._
//  private val rename = FieldSerializer[OneDriveFolderItem](
//    renameTo("@microsoft.graph.downloadUrl", "downloadUrl"),
//    renameFrom("downloadUrl", "@microsoft.graph.downloadUrl")
//  )

  def parseOneDriveItemJson(j:String):OneDriveFolderResp = {
    import org.json4s.jackson.JsonMethods._
    implicit val fmt = DefaultFormats //+ rename
    parse(j).extract[OneDriveFolderResp]
  }
}
