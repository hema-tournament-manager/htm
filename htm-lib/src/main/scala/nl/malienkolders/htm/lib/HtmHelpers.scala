package nl.malienkolders.htm.lib

object HtmHelpers {

  def renderTime(time: Long) = {
    val totalSeconds = (time / 1000f).ceil.toInt
    val minutes = (totalSeconds / 60).toInt
    val seconds = totalSeconds % 60

    "%d:%02d" format (minutes, seconds)
  }

  case class HtmInt(val i: Int) {
    def odd_? = i % 2 == 1
    def even_? = !odd_?
  }

  implicit def int2HtmIntPromotion(i: Int) = HtmInt(i)

}