package nl.malienkolders.htm.battle.lib

case class ViewerSettings(screenIdx: Int, fullscreen: Boolean, width: Int, height: Int)
case class Screen(id: String, width: Int, height: Int, fullscreenSupported: Boolean)
