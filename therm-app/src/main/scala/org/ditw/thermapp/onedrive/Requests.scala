package org.ditw.thermapp.onedrive

import org.apache.http.{HttpHeaders, HttpResponse}
import org.apache.http.client.methods.{HttpGet, HttpUriRequest}
import org.ditw.thermapp.AuthHelper
import org.ditw.thermapp.onedrive.{HttpRespHandler, HttpRespHandlerT}
import org.ditw.thermapp.FolderItem
import org.ditw.thermapp.onedrive.localcache.CacheHelper
import org.ditw.thermapp.onedrive.localcache.CacheHelper.{FileCache, FolderCache}

import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future, duration}
import duration._
import scala.util.{Failure, Success}

/**
  * Created by dev on 2018-08-10.
  */
object Requests {
  val ReqUrlBase = "https://graph.microsoft.com/v1.0/"

  private val ReqDefaultDriveRoot = s"${ReqUrlBase}drive/root/children"

  private def httpGet(uri:String):HttpUriRequest = {
    val res = new HttpGet(uri)
    val authHeader = s"bearer {${AuthHelper.getToken}}"
    res.addHeader(HttpHeaders.AUTHORIZATION, authHeader)
    res
  }

  def reqDriveRoot(respHandler:HttpRespHandler):Unit = {
    HttpHelper.doReq(httpGet(ReqDefaultDriveRoot), respHandler)
  }

  private val ReqRootSharedFolder = s"${ReqUrlBase}shares/s!AkZrjJKFYvCfr36IUg0k7algsNUt/driveItem/children"
  def reqRootSharedFolder(respHandler:HttpRespHandler):Unit = {
    HttpHelper.doReq(httpGet(ReqRootSharedFolder), respHandler)
  }

  private val ReqSharedFolderItems = s"${ReqUrlBase}shares/s!AkZrjJKFYvCfr36IUg0k7algsNUt/items/%s/children"
  def reqSharedFolderItems(sharedFolderId:String, respHandler:HttpRespHandlerT[Array[FolderItem]]):Unit = {
    HttpHelper.doReq(httpGet(ReqSharedFolderItems.format(sharedFolderId)), respHandler)
  }

  private val ReqFolderItems = s"${ReqUrlBase}drive/items/%s/children"
  def reqFolderItems(folderId:String, respHandler:HttpRespHandlerT[Array[FolderItem]]):Unit = {
    HttpHelper.doReq(httpGet(ReqFolderItems.format(folderId)), respHandler)
  }

  private val ReqItemContent = s"${ReqUrlBase}drive/items/%s/content"
  def reqContent(folderItem: FolderItem):Future[HttpResponse] =
    HttpHelper.doReq(httpGet(folderItem.`@microsoft.graph.downloadUrl`.get))
  def reqCacheSharedFolderItems(sharedFolderId:String):Future[FolderCache] = {
    reqCacheSharedItems(ReqSharedFolderItems.format(sharedFolderId), Option(sharedFolderId))
  }
  def reqCacheSharedFolderRootItems():Future[FolderCache] = {
    reqCacheSharedItems(ReqRootSharedFolder, None)
  }

  def reqCacheSharedItems(sharedItemUrl:String, sharedFolderId:Option[String]):Future[FolderCache] = {
    val relPath = sharedFolderId.getOrElse("")
    HttpHelper.doReq(httpGet(sharedItemUrl))
      .map(HttpHelper.handleFolderItems)
      .andThen {
        case Success(folderItems) => {
          println(s"Folder Items: ${folderItems.length}, caching ...")
          if (folderItems.nonEmpty) {
            val allReqs = folderItems
              .filter(_.`@microsoft.graph.downloadUrl`.nonEmpty)
              .toIndexedSeq.map { item =>
                println(s"Caching ${item.name} in folder [$relPath] ...")
                HttpHelper.reqContent(item)
                  .map { bytes =>
                    CacheHelper.cacheFile(bytes, relPath, item.name)
                  }
              }
            val f = Future.sequence(allReqs)
            Await.ready(f, 20 seconds)
            println(s"Done caching [${folderItems.length}] items")
            //folderItems
            //            .onComplete{
            //              case Success(_) => println(s"Done caching [$folderItems/${item0.name}]")
            //              case Failure(t) => println(s"Failed to cache: ${t.getMessage}")
          }
        }
      }
      .map { folderItems =>
        val fileCaches = folderItems.filter(!_.isFolder).map(f => FileCache(relPath, f.name))
        CacheHelper.cacheFolder(relPath, fileCaches)
      }
  }

  def reqCacheFolderItems(folderId:String):Future[FolderCache] = {
    HttpHelper.doReq(httpGet(ReqFolderItems.format(folderId)))
      .map(HttpHelper.handleFolderItems)
      .andThen {
        case Success(folderItems) => {
          println(s"Folder Items: ${folderItems.length}, caching ...")
          if (folderItems.nonEmpty) {
            val allReqs = folderItems.toIndexedSeq.map { item =>
              println(s"Caching ${item.name} in folder [$folderId] ...")
              HttpHelper.reqContent(item)
                .map { bytes =>
                  CacheHelper.cacheFile(bytes, folderId, item.name)
                }
            }
            val f = Future.sequence(allReqs)
            Await.ready(f, 20 seconds)
            println(s"Done caching [${folderItems.length}] items")
            //folderItems
//            .onComplete{
//              case Success(_) => println(s"Done caching [$folderItems/${item0.name}]")
//              case Failure(t) => println(s"Failed to cache: ${t.getMessage}")
          }
        }
      }
      .map { folderItems =>
        val fileCaches = folderItems.filter(!_.isFolder).map(f => FileCache(folderId, f.name))
        CacheHelper.cacheFolder(folderId, fileCaches)
      }
  }
}
