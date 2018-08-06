package org.ditw.learning.akkastr.affcountry

object Tokenizer {

  private val _spaceCommaColonSplitPattern = "[\\h+,;()\"]".r

  def simpleTokenize(input:String, toLower:Boolean):Array[String] = {
    val sp =
      if (toLower)
        _spaceCommaColonSplitPattern.split(input)
//          .filter(!_.isEmpty)
          .map(_.toLowerCase())
      else
        _spaceCommaColonSplitPattern.split(input)
//          .filter(!_.isEmpty)

    sp.map { t => if (t.endsWith(".")) t.substring(0, t.length-1) else t }
      .filter(!_.isEmpty)
  }

}
