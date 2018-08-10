package org.ditw.learning.thermoapp.onedrive

import org.apache.commons.httpclient.HttpClient
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.ditw.learning.javafx.thermoapp.onedrive.{HttpRespHandler, HttpRespHandlerT}
import org.ditw.learning.thermoapp.DataHelpers._
import org.ditw.learning.thermoapp.FolderItem

import concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by dev on 2018-08-10.
  */
object HttpHelper {
  private val client:DefaultHttpClient = new DefaultHttpClient()

  def doReq(req:HttpUriRequest, callback:HttpRespHandler):Unit = {
    val fResp:Future[HttpResponse] = Future{client.execute(req)}
    fResp.onComplete {
      case Success(resp) => {
        callback.handle(resp)
      }
      case Failure(f) => {
        println(s"Request failed: ${f.getMessage}")
      }
    }
  }

  def doReq[T](req:HttpUriRequest, callback:HttpRespHandlerT[T]):Unit = {
    val fResp:Future[HttpResponse] = Future{client.execute(req)}
    fResp.onComplete {
      case Success(resp) => {
        callback.handle(resp)
      }
      case Failure(f) => {
        println(s"Request failed: ${f.getMessage}")
      }
    }
  }
  def handleFolderItems(resp: HttpResponse): Array[FolderItem] = {
    val json = EntityUtils.toString(resp.getEntity)
    val items = parseOneDriveItemJson(json)
    println(items.value.length)
    items.value
  }
  def handleDriveRoot(resp: HttpResponse): Array[FolderItem] = handleFolderItems(resp)
}
