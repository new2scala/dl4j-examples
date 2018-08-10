package org.ditw.learning.thermoapp.onedrive

import org.apache.http.{HttpHeaders, HttpResponse}
import org.apache.http.client.methods.{HttpGet, HttpUriRequest}
import org.ditw.learning.javafx.thermoapp.AuthHelper
import org.ditw.learning.javafx.thermoapp.onedrive.{HttpRespHandler, HttpRespHandlerT}
import org.ditw.learning.thermoapp.FolderItem

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


  private val ReqFolderItems = s"${ReqUrlBase}drive/items/%s/children"
  def reqFolderItems(folderId:String, respHandler:HttpRespHandlerT[Array[FolderItem]]):Unit = {
    HttpHelper.doReq(httpGet(ReqFolderItems.format(folderId)), respHandler)
  }
}
