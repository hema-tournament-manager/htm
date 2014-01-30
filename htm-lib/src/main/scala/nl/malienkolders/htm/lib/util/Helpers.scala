package nl.malienkolders.htm.lib.util

import scala.sys.SystemProperties
import java.awt.Desktop
import java.net.URI

object Helpers {
  implicit class PimpedInt(val i: Int) extends AnyVal {
    def isEven = i % 2 == 0
    def isOdd = !isEven
  }

  def openUrlFromSystemProperty(property: String): Unit = {
    new SystemProperties().get(property).foreach { url =>
      val desktop = if (Desktop.isDesktopSupported()) Some(Desktop.getDesktop()) else None
      desktop foreach {
        case dt if dt.isSupported(Desktop.Action.BROWSE) =>
          dt.browse(URI.create(url))
      }
    }
  }
}