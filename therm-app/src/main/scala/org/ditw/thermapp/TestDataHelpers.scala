package org.ditw.thermapp

import java.io.{File, FileInputStream}
import java.nio.charset.StandardCharsets

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.apache.commons.io
import org.apache.commons.io.IOUtils
import org.ditw.thermapp.DataHelpers.{DataSource, DataUnit}
import org.ditw.thermapp.onedrive.localcache.CacheHelper
import org.json4s.DefaultFormats

import scala.concurrent.Future
import scala.concurrent.duration._
import concurrent.ExecutionContext.Implicits.global
import scala.util.Random

/**
  * Created by dev on 2018-08-11.
  */
object TestDataHelpers {

  private val thermoDataDim = 8

  case class ThermoReading(time:String, pixels:Array[Double]) {
    def data2D:Array[Array[Double]] = {
      val w = 8
      val h = pixels.length/w
      val res = new Array[Array[Double]](h)
      (0 until h).foreach { i =>
        res(i) = new Array[Double](w)
        (0 until w).foreach { j =>
          res(i)(j) = pixels(i*w+j)
        }
      }
      res
    }
  }

  val r = new Random

  def getSelectedDataSource(folderItem: FolderItem):DataSource =  new DataSource {

//    private val dataPath = s"${CacheHelper.driveCache.localPath}/${folderItem.id}"
//    private val extPath = s"$dataPath/_ext"
//    private val imagePath = s"$extPath/ts1_shots"  //todo: path depending on timestamp
//    private val thermoReadingPath = s"$extPath/ts1.json"
    private val dataPath = s"${CacheHelper.driveCache.localPath}"
    //private val extPath = s"$dataPath/_ext"
    private val imagePath = s"$dataPath\\9FF06285928C6B46!6143"  //todo: path depending on timestamp
    private val thermoReadingPath = s"$dataPath\\index.json"

    private val imagesSorted:IndexedSeq[String] = {
      try {
        //new File(imagePath).listFiles().map(f => URLEncoder.encode(f.getName, "utf-8")).toIndexedSeq
        val dir = new File(imagePath)
        if (dir.exists()) {
          dir.listFiles().map(_.getName).toIndexedSeq
        }
        else
          IndexedSeq()
      }
      catch {
        case t:Throwable => {
          throw new RuntimeException(t)
        }
      }
    }

    private val thermoReadings:Array[ThermoReading] = {
      import org.json4s.jackson.JsonMethods._
      implicit val fmt = DefaultFormats
      if (new File(thermoReadingPath).exists()) {
        val s = IOUtils.toString(new FileInputStream(thermoReadingPath), StandardCharsets.UTF_8)
        parse(s).extract[Array[ThermoReading]]
      }
      else
        Array()
    }
    private val thermoData:IndexedSeq[Array[Array[Double]]] = thermoReadings.map(_.data2D)
//    {
//      imagesSorted.map { _ =>
//        val res = new Array[Array[Double]](thermoDataDim)
//        (0 until thermoDataDim).foreach { idx =>
//          res(idx) = new Array[Double](thermoDataDim)
//          val temp = r.nextInt(100)+ThermoColorHelper._ThermoValMin
//          (0 until thermoDataDim).foreach { j =>
//            res(idx)(j) = temp
//          }
//        }
//        res
//      }
//    }

    private var cursor = 0

    private def time2Sec(ts:String):Long = {
      val parts = ts.substring(ts.length-8).split("\\.")
      val hours = parts(0).toInt
      val minutes = parts(1).toInt
      val seconds = parts(2).toInt
      val total:Long = (hours*60+minutes)*60 + seconds
      total
    }

    private def dataUnit(csr:Int):Option[DataUnit] = {
      val imgPath = imagesSorted(csr)
      val thermR = thermoReadings(csr).time
      val thermoTime = time2Sec(thermR)
      val imgTime = time2Sec(imgPath.substring(0, imgPath.length-4))
      println(s"---------------\n\tThermo Reading Time: $thermR\n\t         Image Path: $imgPath\n\t              Diff: ${thermoTime-imgTime}")
      val fileName = s"$imagePath/$imgPath"
      println(s"Getting image [$fileName] ...")
      val fio = new FileInputStream(fileName)
      val res = DataUnit(
        thermoData(csr),
        IOUtils.toByteArray(fio)
      )
      fio.close()
      Option(res)
    }

    override def next: Option[DataUnit] = {
      if (cursor < imagesSorted.size-1) {
        cursor += 1
        dataUnit(cursor)
      }
      else None
    }

    override def moveNext: Unit = {
      if (cursor < imagesSorted.size-1) {
        cursor += 1
      }
    }

    override def movePref: Unit = {
      if (cursor > 0) {
        cursor -= 1
      }
    }

    override def prev: Option[DataUnit] = {
      if (cursor > 0) {
        cursor -= 1
        dataUnit(cursor)
      }
      else None
    }

    override def curr: DataUnit = {
      dataUnit(cursor).get
    }

    def play(tickHandler: PlayTickHandler):Unit = {
      implicit val system = ActorSystem("filesys")
      implicit val mat = ActorMaterializer()
      val indexSource:Source[Int, NotUsed] = Source(cursor until imagesSorted.size)
      val done:Future[Done] = indexSource
        .throttle(1, 0.2 seconds)
        .runWith(
          Sink.foreach { _ =>
            tickHandler.handle()
            moveNext
          }
      )

      done.onComplete(_ => system.terminate())

    }
  }

}
