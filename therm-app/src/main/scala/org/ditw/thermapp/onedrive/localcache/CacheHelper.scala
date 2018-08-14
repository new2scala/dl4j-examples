package org.ditw.thermapp.onedrive.localcache

import java.io.{File, FileOutputStream}

import org.apache.commons.io.IOUtils
import org.ditw.thermapp.FolderItem
import org.ditw.thermapp.onedrive.{FolderCachedRespHandler, HttpHelper, HttpRespHandlerT}
import org.ditw.thermapp.onedrive.Requests.{ReqFolderItems, httpGet}

import scala.util.{Failure, Success}

/**
  * Created by dev on 2018-08-11.
  */
object CacheHelper {
  case class FolderCache(relLocalPath:String, drive:DriveCache, files:Array[FileCache])
  case class DriveCache(localPath:String) {
    private[CacheHelper] val folderCache = FolderCache("/", this, Array())
  }

  case class FileCache(relLocalPath:String, name:String)

  val driveCache = DriveCache("Y:\\vmshare\\onedriveCache")

  def cacheFolder(relLocalPath:String, files:Array[FileCache]):FolderCache = {
    FolderCache(relLocalPath, driveCache, files)
  }

  def cacheFile(bytes:Array[Byte], relLocalPath:String, name:String):Unit = {
    val folder = new File(s"${driveCache.localPath}/$relLocalPath")
    if (!folder.exists())
      folder.mkdirs()
    val os = new FileOutputStream(new File(folder, name))
    IOUtils.write(bytes, os)
    os.close()
  }
}
