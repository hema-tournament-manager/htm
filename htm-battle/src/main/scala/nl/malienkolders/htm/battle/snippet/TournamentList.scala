package nl.malienkolders.htm.battle.snippet

import net.liftweb._
import http._
import util.Helpers._
import json._
import nl.malienkolders.htm.lib.model._

object TournamentList {

  implicit val formats = Serialization.formats(NoTypeHints)

  def render = {

    val ts = Serialization.read[List[MarshalledTournament]]("")
    "#tournament *" #> ts.map(_.name)
  }

}