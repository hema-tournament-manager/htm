package nl.malienkolders.htm.viewer.jmonkey.lib

case class ViewerSettings(screenIdx: Int, fullscreen: Boolean, size: Option[(Int, Int)])
case class Screen(id: String, width: Int, height: Int, fullscreenSupported: Boolean)
