package nl.malienkolders.htm.lib

object HtmHelpers {

  def renderTime(time: Long) = {
    val totalSeconds = (time / 1000f).ceil.toInt
    val minutes = (totalSeconds / 60).toInt
    val seconds = totalSeconds % 60

    "%d:%02d" format (minutes, seconds)
  }

}