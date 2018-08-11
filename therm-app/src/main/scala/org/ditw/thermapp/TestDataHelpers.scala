package org.ditw.thermapp

import java.io.{File, FileInputStream}

import org.apache.commons.io.IOUtils
import org.ditw.thermapp.DataHelpers.{DataSource, DataUnit}
import org.ditw.thermapp.onedrive.localcache.CacheHelper

import scala.util.Random

/**
  * Created by dev on 2018-08-11.
  */
object TestDataHelpers {

  private val thermoDataDim = 8

  val r = new Random

  def getSelectedDataSource(folderItem: FolderItem):DataSource =  new DataSource {
    private val imagePath = s"${CacheHelper.driveCache.localPath}/${folderItem.id}"

    private val imagesSorted:IndexedSeq[String] = {
      try {
        //new File(imagePath).listFiles().map(f => URLEncoder.encode(f.getName, "utf-8")).toIndexedSeq
        val dir = new File(imagePath)
        dir.listFiles().map(_.getName).toIndexedSeq
      }
      catch {
        case t:Throwable => {
          throw new RuntimeException(t)
        }
      }
    }


    private val thermoData:IndexedSeq[Array[Array[Double]]] = {
      imagesSorted.map { _ =>
        val res = new Array[Array[Double]](thermoDataDim)
        (0 until thermoDataDim).foreach { idx =>
          res(idx) = new Array[Double](thermoDataDim)
          val temp = r.nextInt(100)+ThermoColorHelper._ThermoValMin
          (0 until thermoDataDim).foreach { j =>
            res(idx)(j) = temp
          }
        }
        res
      }
    }

    private var cursor = 0

    private def dataUnit(csr:Int):Option[DataUnit] = {
      val fileName = s"$imagePath/${imagesSorted(csr)}"
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
  }

}
