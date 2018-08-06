package org.ditw.learning.akkastr

import java.io.File
import java.net.URLEncoder

class FolderNav(val folder:File) {

  val sortedChildren:IndexedSeq[File] =
    folder.listFiles().toIndexedSeq.sortBy(_.getAbsolutePath)

  private var cursor:Int = 0

  def curr():File = {
    val res = sortedChildren(cursor)
    res
  }

  def currUrl():String = {
    val p = curr().getName
    val res = curr().getParent + '/' + URLEncoder.encode(p, "utf-8")
    res
  }

  def next():Unit =
    if (cursor < sortedChildren.size-1) {
      cursor += 1
    }
  def prev():Unit = if (cursor > 0) {
    cursor -= 1
  }


}
