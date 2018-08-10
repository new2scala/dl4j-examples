package org.ditw.learning.thermoapp

import java.util.UUID


/**
  * Created by dev on 2018-08-10.
  */
case class FolderItem(
  id:String,
  name:String,
  //downloadUrl:String
  `@microsoft.graph.downloadUrl`:Option[String],
  folder:Option[FolderItemFolder]
) {
  override def toString: String = if (folder.nonEmpty) s"［$name］" else name
  def isRoot:Boolean = id == FolderItems.ROOT.id
  def isFolder:Boolean = folder.nonEmpty
}

case class FolderItemFolder(
  childCount:Int
)

case class FolderResp(
  value:Array[FolderItem]
) {
  private val _downloadLinks = value.sortBy(_.name).map(_.`@microsoft.graph.downloadUrl`)
  //    def downloadLinks:Array[String] = _downloadLinks
}

object FolderItems {
  private val idRoot = UUID.randomUUID().toString;
  val ROOT = FolderItem(idRoot, "［_ROOT_］", None, None)
}
