package nl.malienkolders.htm.lib.util

object Helpers {
  implicit class PimpedInt(val i: Int) extends AnyVal {
    def isEven = i % 2 == 0
    def isOdd = !isEven
  }
}