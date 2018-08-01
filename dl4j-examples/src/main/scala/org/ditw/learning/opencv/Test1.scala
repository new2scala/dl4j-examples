package org.ditw.learning.opencv

import org.opencv.core.{Core, CvType, Mat}


object Test1 extends App {

  System.loadLibrary(Core.NATIVE_LIBRARY_NAME)

  val ver = Core.VERSION

  println(ver)

  System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
  val mat = Mat.eye(3, 3, CvType.CV_8UC1)
  System.out.println("mat = " + mat.dump)
}
