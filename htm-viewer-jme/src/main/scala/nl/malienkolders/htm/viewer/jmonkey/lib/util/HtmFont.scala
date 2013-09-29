package nl.malienkolders.htm.viewer.jmonkey.lib.util

import java.awt.Font

abstract class HtmFont {
  def load(): Font
}

class SystemFont(fontName: String) extends HtmFont {
  def load(): Font = {
    new Font(fontName, Font.PLAIN, 10)
  }
}

class ResourceFont(resourceName: String) extends HtmFont {
  def load(): Font = {
    val in = getClass().getResourceAsStream(s"/Interface/Fonts/${resourceName}")
    val font = Font.createFont(Font.TRUETYPE_FONT, in)
    in.close()
    font
  }
}

case object Dialog extends SystemFont(Font.DIALOG)
case object Arial extends ResourceFont("arialbd.ttf")
case object Copperplate extends ResourceFont("coprgtb.ttf")
case object Raavi extends ResourceFont("raavib.ttf")

