package org.ditw.learning.thermoapp

import java.io.{File, FileInputStream}
import java.net.URLEncoder

import org.apache.commons.io.IOUtils
import org.ditw.learning.javafx.thermoapp.ThermoColorHelper
import org.ditw.learning.thermoapp.DataHelpers._

import scala.util.Random

object TestDataHelpers {

  private val thermoDataDim = 8

  val r = new Random

  val mockDataSource = new DataSource {
    private val imagePath = "/media/sf_vmshare/Icons64"

    private val imagesSorted:IndexedSeq[String] = {
      try {
        //new File(imagePath).listFiles().map(f => URLEncoder.encode(f.getName, "utf-8")).toIndexedSeq
        new File(imagePath).listFiles().map(_.getName).toIndexedSeq
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
