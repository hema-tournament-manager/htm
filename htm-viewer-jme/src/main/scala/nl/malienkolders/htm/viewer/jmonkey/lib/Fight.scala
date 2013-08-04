package nl.malienkolders.htm.viewer.jmonkey.lib

case class Fight(a: Fighter, b: Fighter, scoreA: Int, scoreB: Int, doubleHits: Int, time: Long)
case class Fighter(name: String, club: String, country: String, avatar: String)
