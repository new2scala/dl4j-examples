package org.ditw.learning.onedrive

import java.nio.charset.StandardCharsets

import org.apache.http.client.config.{CookieSpecs, RequestConfig}
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.impl.client.{HttpClients, LaxRedirectStrategy}
import org.apache.http.util.EntityUtils

object AuthTest extends App {

  println("Trying to get token")

  val req = "https://login.live.com/oauth20_authorize.srf?client_id=dcfbb7e5-d75f-4726-9201-bb35a438ef9b&scope=files.read&response_type=token&redirect_uri=https://login.live.com/oauth20_desktop.srf"

  import org.apache.http.client.methods.HttpGet

  val httpget = new HttpGet(req)

  import org.apache.http.impl.client.DefaultHttpClient

  val cookieSpec = CookieSpecs.BROWSER_COMPATIBILITY
  val reqConf = RequestConfig.custom()
    .setCookieSpec(cookieSpec)
    .setExpectContinueEnabled(true)
    .build()

  val httpclient = HttpClients.custom()
    .setRedirectStrategy(new LaxRedirectStrategy())
    .setDefaultRequestConfig(reqConf)
    .build()
  val context = HttpClientContext.create()
  val response = httpclient.execute(httpget, context)
  val cookies = context.getCookieOrigin
  println(cookies)

  import org.apache.http.HttpEntity

  val entity = response.getEntity

  val respStr = EntityUtils.toString(entity, StandardCharsets.UTF_8)
  println(respStr)
//  import collection.JavaConverters._
//
//  httpclient.getCookies.asScala.foreach { c =>
//    println(c.toString)
//  }
//
//  println(httpclient.getCookieStore.getCookies.size)

}
