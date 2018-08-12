package org.ditw.thermapp

import org.json4s.DefaultFormats

object DataHelpers {
  case class DataUnit(
    thermoGrid:Array[Array[Double]],
    imageRaw:Array[Byte]
  )

  trait DataSource {
    def curr:DataUnit
    def movePref:Unit
    def next:Option[DataUnit]
    def moveNext:Unit
    def prev:Option[DataUnit]
    def play(tickHandler: PlayTickHandler):Unit
  }
//  private val rename = FieldSerializer[OneDriveFolderItem](
//    renameTo("@microsoft.graph.downloadUrl", "downloadUrl"),
//    renameFrom("downloadUrl", "@microsoft.graph.downloadUrl")
//  )

  def parseOneDriveItemJson(j:String):FolderResp = {
    implicit val fmt = DefaultFormats //+ rename
    import org.json4s.jackson.JsonMethods._
    parse(j).extract[FolderResp]
  }
}
